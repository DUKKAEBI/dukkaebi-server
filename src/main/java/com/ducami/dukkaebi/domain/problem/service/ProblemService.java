package com.ducami.dukkaebi.domain.problem.service;

import com.ducami.dukkaebi.domain.problem.domain.Problem;
import com.ducami.dukkaebi.domain.problem.domain.ProblemHistory;
import com.ducami.dukkaebi.domain.problem.domain.ProblemTestCase;
import com.ducami.dukkaebi.domain.problem.domain.enums.DifficultyType;
import com.ducami.dukkaebi.domain.problem.domain.repo.ProblemHistoryJpaRepo;
import com.ducami.dukkaebi.domain.problem.domain.repo.ProblemJpaRepo;
import com.ducami.dukkaebi.domain.problem.domain.repo.ProblemTestCaseJpaRepo;
import com.ducami.dukkaebi.domain.problem.presentation.dto.response.ProblemDetailRes;
import com.ducami.dukkaebi.domain.problem.presentation.dto.response.ProblemRes;
import com.ducami.dukkaebi.global.security.auth.UserSessionHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProblemService {
    private final ProblemJpaRepo problemJpaRepo;
    private final ProblemHistoryJpaRepo problemHistoryJpaRepo;
    private final ProblemTestCaseJpaRepo problemTestCaseJpaRepo;
    private final UserSessionHolder userSessionHolder;

    /**
     * 모든 문제 조회
     */
    public List<ProblemRes> getAllProblems() {
        try {
            log.info("모든 문제 조회 시작");

            Long userId = userSessionHolder.getUserId();
            log.info("조회 userId: {}", userId);

            // 유저의 모든 히스토리 조회
            List<ProblemHistory> historyList = problemHistoryJpaRepo.findByUser_Id(userId);
            log.info("히스토리 조회 완료 - {}개", historyList.size());

            // 문제 ID → 히스토리 매핑
            Map<Long, ProblemHistory> historyMap = new HashMap<>();
            for (ProblemHistory history : historyList) {
                if (history.getProblem() != null) {
                    historyMap.put(history.getProblem().getProblemId(), history);
                }
            }

            // 모든 문제 조회 (대회 전용 문제 제외)
            List<Problem> problems = problemJpaRepo.findByContestIdIsNull();
            log.info("문제 조회 완료 - 총 {}개", problems.size());

            // DTO 변환
            return problems.stream()
                    .map(problem -> {
                        ProblemHistory history = historyMap.get(problem.getProblemId());
                        return ProblemRes.from(problem, history);
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("모든 문제 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("문제 목록 조회에 실패했습니다.", e);
        }
    }

    /**
     * 문제 상세 조회
     */
    public ProblemDetailRes getProblem(Long problemId) {
        try {
            log.info("문제 상세 조회 - problemId: {}", problemId);

            // 1. 문제 조회
            Problem problem = problemJpaRepo.findById(problemId)
                    .orElseThrow(() -> new IllegalArgumentException("문제를 찾을 수 없습니다. ID: " + problemId));

            // 2. 테스트케이스 조회
            List<ProblemTestCase> testCases = problemTestCaseJpaRepo.findByProblem_ProblemId(problemId);

            // 3. DTO 변환
            ProblemDetailRes response = ProblemDetailRes.from(problem, testCases);

            log.info("문제 상세 조회 완료 - problemId: {}, testCases: {}개", problemId, testCases.size());
            return response;

        } catch (Exception e) {
            log.error("문제 상세 조회 실패 - problemId: {}, error: {}", problemId, e.getMessage(), e);
            throw new RuntimeException("문제 상세 조회에 실패했습니다.", e);
        }
    }

    /**
     * 필터링된 문제 조회
     */
    public List<ProblemRes> getProblemsWithFilter(DifficultyType difficulty, String time, String correctRate) {
        try {
            log.info("필터링된 문제 조회 - difficulty: {}, time: {}, correctRate: {}", difficulty, time, correctRate);

            Long userId = userSessionHolder.getUserId();

            // 유저의 모든 문제 히스토리 조회
            List<ProblemHistory> historyList = problemHistoryJpaRepo.findByUser_Id(userId);

            Map<Long, ProblemHistory> historyMap = new HashMap<>();
            for (ProblemHistory history : historyList) {
                if (history.getProblem() != null) {
                    historyMap.put(history.getProblem().getProblemId(), history);
                }
            }

            // 1. 난이도 필터 적용 (대회 전용 문제 제외)
            List<Problem> problems;
            if (difficulty != null) {
                // 난이도로 필터링하되, contestId가 null인 것만
                problems = problemJpaRepo.findByDifficulty(difficulty).stream()
                        .filter(p -> p.getContestId() == null)
                        .collect(Collectors.toList());
                log.info("난이도 필터 적용 - {}: {}개", difficulty, problems.size());
            } else {
                problems = problemJpaRepo.findByContestIdIsNull();
                log.info("난이도 필터 없음 - 전체: {}개", problems.size());
            }

            // 2. Entity -> DTO 변환
            List<ProblemRes> problemResList = problems.stream()
                    .map(problem -> ProblemRes.from(problem, historyMap.get(problem.getProblemId())))
                    .collect(Collectors.toList());

            // 3. 정렬 적용 (우선순위: 정답률 > 시간)
            if (correctRate != null && !correctRate.isBlank()) {
                problemResList = sortByCorrectRate(problemResList, correctRate);
                log.info("정답률 정렬 적용 - {}", correctRate);
            } else if (time != null && !time.isBlank()) {
                problemResList = sortByTime(problemResList, time);
                log.info("시간 정렬 적용 - {}", time);
            }

            log.info("필터링된 문제 조회 완료 - 총 {}개", problemResList.size());
            return problemResList;

        } catch (Exception e) {
            log.error("필터링된 문제 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("필터링된 문제 조회에 실패했습니다.", e);
        }
    }

    /**
     * 이름으로 문제 검색
     */
    public List<ProblemRes> getProblemsWithName(String name) {
        try {
            log.info("문제 이름으로 조회 - name: {}", name);

            if (name == null || name.isBlank()) {
                log.warn("검색어가 비어있습니다.");
                return Collections.emptyList();
            }

            Long userId = userSessionHolder.getUserId();

            // 유저의 모든 문제 히스토리 조회
            List<ProblemHistory> historyList = problemHistoryJpaRepo.findByUser_Id(userId);

            Map<Long, ProblemHistory> historyMap = new HashMap<>();
            for (ProblemHistory history : historyList) {
                if (history.getProblem() != null) {
                    historyMap.put(history.getProblem().getProblemId(), history);
                }
            }

            // 이름으로 문제 검색 (대회 전용 문제 제외)
            List<Problem> problems = problemJpaRepo.searchByName(name).stream()
                    .filter(p -> p.getContestId() == null)
                    .collect(Collectors.toList());
            log.info("검색 결과 - {}개", problems.size());

            return problems.stream()
                    .map(problem -> ProblemRes.from(problem, historyMap.get(problem.getProblemId())))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("이름으로 문제 검색 실패 - name: {}, error: {}", name, e.getMessage(), e);
            throw new RuntimeException("문제 검색에 실패했습니다.", e);
        }
    }

    /**
     * 정답률 기준 정렬
     */
    private List<ProblemRes> sortByCorrectRate(List<ProblemRes> problems, String correctRate) {
        Comparator<ProblemRes> comparator = Comparator.comparing(
                ProblemRes::correctRate,
                Comparator.nullsLast(Double::compareTo)  // null 안전 처리
        );

        // "high" = 높은 순, "low" = 낮은 순
        if ("low".equalsIgnoreCase(correctRate)) {
            comparator = comparator.reversed();
        }

        return problems.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    /**
     * 시간 기준 정렬
     */
    private List<ProblemRes> sortByTime(List<ProblemRes> problems, String time) {
        Comparator<ProblemRes> comparator = Comparator.comparing(
                ProblemRes::addedAt,
                Comparator.nullsLast(Comparator.naturalOrder())  // null 안전 처리
        );

        // "recent" = 최신순, "old" = 오래된순
        if ("recent".equalsIgnoreCase(time)) {
            comparator = comparator.reversed();
        }

        return problems.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }
}