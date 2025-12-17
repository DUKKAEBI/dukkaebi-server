package com.ducami.dukkaebi.domain.grading.service;

import com.ducami.dukkaebi.domain.grading.domain.enums.JudgeStatus;
import com.ducami.dukkaebi.domain.grading.model.ExecutionResult;
import com.ducami.dukkaebi.domain.grading.presentation.dto.response.JudgeResultRes;
import com.ducami.dukkaebi.domain.grading.util.CodeExecutor;
import com.ducami.dukkaebi.domain.problem.domain.Problem;
import com.ducami.dukkaebi.domain.problem.domain.ProblemHistory;
import com.ducami.dukkaebi.domain.problem.domain.ProblemTestCase;
import com.ducami.dukkaebi.domain.problem.domain.enums.DifficultyType;
import com.ducami.dukkaebi.domain.problem.domain.enums.SolvedResult;
import com.ducami.dukkaebi.domain.problem.domain.repo.ProblemHistoryJpaRepo;
import com.ducami.dukkaebi.domain.problem.domain.repo.ProblemJpaRepo;
import com.ducami.dukkaebi.domain.problem.domain.repo.ProblemTestCaseJpaRepo;
import com.ducami.dukkaebi.domain.user.domain.User;
import com.ducami.dukkaebi.domain.user.domain.repo.UserJpaRepo;
import com.ducami.dukkaebi.domain.user.service.UserActivityService;
import com.ducami.dukkaebi.global.security.auth.UserSessionHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JudgeService {
    private final ProblemJpaRepo problemJpaRepo;
    private final ProblemTestCaseJpaRepo testCaseJpaRepo;
    private final ProblemHistoryJpaRepo problemHistoryJpaRepo;
    private final CodeExecutor codeExecutor;
    private final UserSessionHolder userSessionHolder;
    private final UserJpaRepo userJpaRepo;
    private final UserActivityService userActivityService;

    /**
     * 백준 스타일 코드 채점
     * 1. 모든 테스트케이스에 대해 코드 실행
     * 2. 출력 비교
     * 3. 결과 반환 (+ 정답 시 점수 부여)
     */

    @Transactional // 점수 업데이트를 위해 쓰기 트랜잭션
    public JudgeResultRes judgeCode(Long problemId, String code, String language) {
        log.info("코드 채점 시작 - problemId: {}, language: {}", problemId, language);

        // 1. 문제 조회
        Problem problem = problemJpaRepo.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("문제를 찾을 수 없습니다."));

        // 2. 테스트케이스 조회
        List<ProblemTestCase> testCases = testCaseJpaRepo.findByProblem_ProblemId(problemId);

        if (testCases.isEmpty()) {
            throw new IllegalStateException("테스트케이스가 없습니다.");
        }

        // 3. 각 테스트케이스 실행
        List<JudgeResultRes.TestCaseResult> results = new ArrayList<>();
        int passedCount = 0;
        long totalExecutionTime = 0;
        JudgeStatus finalStatus = JudgeStatus.ACCEPTED;
        String errorMessage = null;

        for (int i = 0; i < testCases.size(); i++) {
            ProblemTestCase testCase = testCases.get(i);

            try {
                long startTime = System.currentTimeMillis();

                // 코드 실행 (5초 타임아웃)
                ExecutionResult result = codeExecutor.execute(
                        code,
                        language,
                        testCase.getInput(),
                        5000
                );

                long executionTime = System.currentTimeMillis() - startTime;
                totalExecutionTime += executionTime;

                // 실행 실패 체크
                if (!result.success()) {
                    finalStatus = JudgeStatus.RUNTIME_ERROR;
                    errorMessage = result.error();

                    results.add(new JudgeResultRes.TestCaseResult(
                            i + 1, false, testCase.getOutput(), result.error()
                    ));
                    break;
                }

                // 타임아웃 체크
                if (result.timeout()) {
                    finalStatus = JudgeStatus.TIME_LIMIT_EXCEEDED;
                    errorMessage = "시간 초과";

                    results.add(new JudgeResultRes.TestCaseResult(
                            i + 1, false, testCase.getOutput(), "시간 초과"
                    ));
                    break;
                }

                // 출력 비교 (공백, 줄바꿈 정규화)
                String expected = normalizeOutput(testCase.getOutput());
                String actual = normalizeOutput(result.output());
                boolean passed = expected.equals(actual);

                if (passed) {
                    passedCount++;
                } else {
                    finalStatus = JudgeStatus.WRONG_ANSWER;
                }

                results.add(new JudgeResultRes.TestCaseResult(
                        i + 1,
                        passed,
                        testCase.getOutput(),
                        result.output()
                ));

                // 첫 번째 오답에서 멈춤 (백준 스타일)
                if (!passed) {
                    break;
                }

            } catch (Exception e) {
                log.error("테스트케이스 실행 중 예외 발생: {}", e.getMessage(), e);
                finalStatus = JudgeStatus.RUNTIME_ERROR;
                errorMessage = e.getMessage();

                results.add(new JudgeResultRes.TestCaseResult(
                        i + 1, false, testCase.getOutput(), "실행 에러: " + e.getMessage()
                ));
                break;
            }
        }

        // 정답 처리 시 점수 부여
        if (finalStatus == JudgeStatus.ACCEPTED) {
            int reward = difficultyToScore(problem.getDifficulty());
            try {
                User sessionUser = userSessionHolder.getUser();
                User user = userJpaRepo.findById(sessionUser.getId())
                        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
                user.addScore(reward);
                // 일일 활동 1 증가 (정답 1건)
                userActivityService.increaseTodaySolvedCount(1);
                log.info("점수/활동 갱신 - userId: {}, +{}점, 오늘 푼 문제 +1", user.getId(), reward);
            } catch (Exception e) {
                log.error("점수/활동 갱신 실패: {}", e.getMessage(), e);
            }
        }

        // ProblemHistory 업데이트 (제출 여부 기록)
        try {
            User sessionUser = userSessionHolder.getUser();
            User user = userJpaRepo.findById(sessionUser.getId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            // 기존 히스토리 조회 또는 새로 생성
            ProblemHistory history = problemHistoryJpaRepo
                    .findByUser_IdAndProblem_ProblemId(user.getId(), problemId)
                    .orElse(ProblemHistory.builder()
                            .user(user)
                            .problem(problem)
                            .solvedResult(SolvedResult.NOT_SOLVED)
                            .build());

            // 정답이면 SOLVED, 오답이면 FAILED로 업데이트
            SolvedResult result = (finalStatus == JudgeStatus.ACCEPTED)
                    ? SolvedResult.SOLVED
                    : SolvedResult.FAILED;
            history.updateSolvedResult(result);

            problemHistoryJpaRepo.save(history);
            log.info("ProblemHistory 업데이트 - userId: {}, problemId: {}, result: {}",
                    user.getId(), problemId, result);
        } catch (Exception e) {
            log.error("ProblemHistory 업데이트 실패: {}", e.getMessage(), e);
        }

        log.info("채점 완료 - status: {}, passed: {}/{}", finalStatus, passedCount, testCases.size());

        return new JudgeResultRes(
                finalStatus,
                passedCount,
                testCases.size(),
                totalExecutionTime,
                errorMessage,
                results
        );
    }

    private int difficultyToScore(DifficultyType difficulty) {
        return switch (difficulty) {
            case COPPER -> 1;   // 구리
            case IRON -> 3;     // 철
            case SLIVER -> 5;   // 은 (오타 주의: SLIVER)
            case GOLD -> 10;    // 금
            case JADE -> 15;    // 옥
        };
    }

    /**
     * 출력 정규화 (공백, 줄바꿈 처리)
     */
    private String normalizeOutput(String output) {
        if (output == null) return "";
        return output.trim()
                .replaceAll("\\r\\n", "\n")  // Windows 줄바꿈 통일
                .replaceAll("\\s+$", "");     // 끝 공백 제거
    }
}