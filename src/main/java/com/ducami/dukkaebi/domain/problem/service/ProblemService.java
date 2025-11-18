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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemService {
    private final ProblemJpaRepo problemJpaRepo;
    private final ProblemHistoryJpaRepo problemHistoryJpaRepo;
    private final ProblemTestCaseJpaRepo problemTestCaseJpaRepo;
    private final UserSessionHolder userSessionHolder;

    public List<ProblemRes> getAllProblems() {
        log.info("모든 문제 조회 시작");

        Long userId = userSessionHolder.getUserId();

        // 유저의 모든 히스토리 조회
        List<ProblemHistory> historyList = problemHistoryJpaRepo.findByUser_Id(userId);

        // 문제 ID → 히스토리 매핑
        Map<Long, ProblemHistory> historyMap = historyList.stream()
                .collect(Collectors.toMap(
                        ph -> ph.getProblem().getProblemId(),
                        ph -> ph
                ));

        List<Problem> problems = problemJpaRepo.findAll();
        log.info("문제 조회 완료 - 총 {}개", problems.size());

        return problems.stream()
                .map(problem -> {
                    ProblemHistory history = historyMap.get(problem.getProblemId());
                    return ProblemRes.from(problem, history);
                })
                .collect(Collectors.toList());
    }

    public ProblemDetailRes getProblem(Long problemId) {
        log.info("문제 상세 조회 - problemId: {}", problemId);

        // 1. 문제 조회
        Problem problem = problemJpaRepo.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("문제를 찾을 수 없습니다. ID: " + problemId));

        // 2. 테스트케이스 조회 (문제에 연관된 모든 테스트케이스)
        List<ProblemTestCase> testCases = problemTestCaseJpaRepo.findByProblem_ProblemId(problemId);

        // 3. DTO 변환 (첫 번째 테스트케이스를 예시로 사용)
        ProblemDetailRes response = ProblemDetailRes.from(problem, testCases);

        log.info("문제 상세 조회 완료 - problemId: {}, testCases: {}개", problemId, testCases.size());
        return response;
    }


    public List<ProblemRes> getProblemsWithFilter(DifficultyType difficulty, String time, String correctRate) {
        log.info("문제 조회 - difficulty: {}, time: {}, correctRate: {}", difficulty, time, correctRate);

        Long userId = userSessionHolder.getUserId();

        // 유저의 모든 문제 히스토리 조회
        List<ProblemHistory> historyList = problemHistoryJpaRepo.findByUser_Id(userId);
        Map<Long, ProblemHistory> historyMap = historyList.stream()
                .collect(Collectors.toMap(
                        ph -> ph.getProblem().getProblemId(),
                        ph -> ph
                ));

        // 1. 난이도 필터 적용
        List<Problem> problems = (difficulty != null)
                ? problemJpaRepo.findByDifficulty(difficulty)
                : problemJpaRepo.findAll();

        // 2. Entity -> DTO 변환 (문제별 히스토리 매칭)
        List<ProblemRes> problemResList = problems.stream()
                .map(problem -> ProblemRes.from(problem, historyMap.get(problem.getProblemId())))
                .collect(Collectors.toList());

        // 3. 정렬 적용 (우선순위: 정답률 > 시간)
        if (correctRate != null && !correctRate.isBlank()) {
            problemResList = sortByCorrectRate(problemResList, correctRate);
        } else if (time != null && !time.isBlank()) {
            problemResList = sortByTime(problemResList, time);
        }

        log.info("문제 조회 완료 - 총 {}개", problemResList.size());
        return problemResList;
    }

    public List<ProblemRes> getProblemsWithName(String name) {
        log.info("문제 이름으로 조회 - {}", name);

        Long userId = userSessionHolder.getUserId();

        // 유저의 모든 문제 히스토리 조회
        List<ProblemHistory> historyList = problemHistoryJpaRepo.findByUser_Id(userId);
        Map<Long, ProblemHistory> historyMap = historyList.stream()
                .collect(Collectors.toMap(
                        ph -> ph.getProblem().getProblemId(),
                        ph -> ph
                ));

        List<Problem> problems = problemJpaRepo.searchByName(name);

        return problems.stream()
                .map(problem -> ProblemRes.from(problem, historyMap.get(problem.getProblemId())))
                .collect(Collectors.toList());
    }

    /**
     * 정답률 기준 정렬
     */
    private List<ProblemRes> sortByCorrectRate(List<ProblemRes> problems, String correctRate) {
        Comparator<ProblemRes> comparator = Comparator.comparing(ProblemRes::correctRate);

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
        Comparator<ProblemRes> comparator = Comparator.comparing(ProblemRes::addedAt);

        if ("recent".equalsIgnoreCase(time)) {
            comparator = comparator.reversed();
        }

        return problems.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }
}
