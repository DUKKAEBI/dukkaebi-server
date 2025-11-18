package com.ducami.dukkaebi.domain.problem.usecase;

import com.ducami.dukkaebi.domain.problem.domain.Problem;
import com.ducami.dukkaebi.domain.problem.error.ProblemErrorCode;
import com.ducami.dukkaebi.domain.problem.presentation.dto.request.ProblemFilterReq;
import com.ducami.dukkaebi.domain.problem.presentation.dto.response.ProblemRes;
import com.ducami.dukkaebi.domain.problem.service.ProblemService;
import com.ducami.dukkaebi.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemUseCase {
    private final ProblemService problemService;

    public List<ProblemRes> getProblem() {
        try {
            log.info("문제 목록 조회 요청");

            List<Problem> problems = problemService.getAllProblems();
            List<ProblemRes> response = problems.stream()
                    .map(ProblemRes::from)
                    .collect(Collectors.toList());

            log.info("문제 목록 조회 성공 - {}개", response.size());
            return response;

        } catch (Exception e) {
            log.error("문제 목록 조회 실패: {}", e.getMessage(), e);
            throw new CustomException(ProblemErrorCode.PROBLEM_FETCH_FAILED);
        }
    }

    public List<ProblemRes> getProblemWithFilter(ProblemFilterReq filter) {
        // Service에서 바로 ProblemRes 리스트 받음
        return problemService.getProblemsWithFilter(
                filter.difficulty(),
                filter.time(),
                filter.correctRate()
        );
    }

    public List<ProblemRes> getProblemWithName(String name) {
        return problemService.getProblemsWithName(name);
    }
}
