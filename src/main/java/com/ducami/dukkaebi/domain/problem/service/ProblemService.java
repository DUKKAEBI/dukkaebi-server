package com.ducami.dukkaebi.domain.problem.service;

import com.ducami.dukkaebi.domain.problem.domain.Problem;
import com.ducami.dukkaebi.domain.problem.domain.enums.DifficultyType;
import com.ducami.dukkaebi.domain.problem.domain.repo.ProblemJpaRepo;
import com.ducami.dukkaebi.domain.problem.presentation.dto.response.ProblemRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemService {

    private final ProblemJpaRepo problemJpaRepo;

    public List<Problem> getAllProblems() {
        log.info("모든 문제 조회 시작");
        List<Problem> problems = problemJpaRepo.findAll();
        log.info("문제 조회 완료 - 총 {}개", problems.size());
        return problems;
    }

    public List<ProblemRes> getProblemsWithFilter(DifficultyType difficulty, String time, String correctRate) {
        log.info("문제 조회 - difficulty: {}, time: {}, correctRate: {}", difficulty, time, correctRate);

        // 1. 난이도 필터 적용
        List<Problem> problems = (difficulty != null)
                ? problemJpaRepo.findByDifficulty(difficulty)
                : problemJpaRepo.findAll();

        // 2. Entity -> DTO 변환
        List<ProblemRes> problemResList = problems.stream()
                .map(ProblemRes::from)
                .collect(Collectors.toList());

        // 3. 정렬 적용 (우선순위: 정답률 > 시간)
        if (correctRate != null && !correctRate.isBlank()) {
            // 정답률 정렬
            problemResList = sortByCorrectRate(problemResList, correctRate);
        } else if (time != null && !time.isBlank()) {
            // 시간 정렬
            problemResList = sortByTime(problemResList, time);
        }

        log.info("문제 조회 완료 - 총 {}개", problemResList.size());
        return problemResList;
    }

    public List<ProblemRes> getProblemsWithName(String name) {
        log.info("문제 이름으로 조회 - {}", name);

        List<Problem> problems = problemJpaRepo.searchByName(name);

        return problems.stream()
                .map(ProblemRes::from)
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

        if ("latest".equalsIgnoreCase(time)) {
            comparator = comparator.reversed();
        }

        return problems.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }
}
