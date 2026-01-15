package com.ducami.dukkaebi.domain.problem.usecase;

import com.ducami.dukkaebi.domain.problem.domain.Problem;
import com.ducami.dukkaebi.domain.problem.domain.ProblemTestCase;
import com.ducami.dukkaebi.domain.problem.domain.enums.DifficultyType;
import com.ducami.dukkaebi.domain.problem.domain.repo.ProblemJpaRepo;
import com.ducami.dukkaebi.domain.problem.domain.repo.ProblemTestCaseJpaRepo;
import com.ducami.dukkaebi.domain.problem.error.ProblemErrorCode;
import com.ducami.dukkaebi.domain.problem.presentation.dto.request.ProblemCreateReq;
import com.ducami.dukkaebi.domain.problem.presentation.dto.request.ProblemUpdateReq;
import com.ducami.dukkaebi.domain.problem.presentation.dto.response.ProblemDetailRes;
import com.ducami.dukkaebi.domain.problem.presentation.dto.response.ProblemRes;
import com.ducami.dukkaebi.domain.problem.service.ProblemService;
import com.ducami.dukkaebi.global.common.dto.response.PageResponse;
import com.ducami.dukkaebi.global.common.dto.response.Response;
import com.ducami.dukkaebi.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemUseCase {
    private final ProblemService problemService;
    private final ProblemJpaRepo problemJpaRepo;
    private final ProblemTestCaseJpaRepo testCaseJpaRepo;

    public PageResponse<ProblemRes> getProblemPaged(int page, int size) {
        try {
            log.info("문제 목록 페이징 조회 요청");

            Pageable pageable = PageRequest.of(page, size);
            Page<ProblemRes> problems = problemService.getAllProblemsPaged(pageable);

            log.info("문제 목록 페이징 조회 성공 - {}개", problems.getTotalElements());
            return PageResponse.of(problems);

        } catch (Exception e) {
            log.error("문제 목록 페이징 조회 실패: {}", e.getMessage(), e);
            throw new CustomException(ProblemErrorCode.PROBLEM_FETCH_FAILED);
        }
    }

    public List<ProblemRes> getProblemWithFilter(DifficultyType difficulty, String time, String correctRate) {
        // Service에서 바로 ProblemRes 리스트 받음
        return problemService.getProblemsWithFilter(
                difficulty,
                time,
                correctRate
        );
    }

    public List<ProblemRes> getProblemWithName(String name) {
        return problemService.getProblemsWithName(name);
    }

    public ProblemDetailRes getOneProblem(Long problemId) {
        try {
            log.info("문제 조회 요청");

            ProblemDetailRes problem = problemService.getProblem(problemId);

            log.info("문제 조회 성공");

            return problem;

        } catch (Exception e) {
            log.error("문제 조회 실패: {}", e.getMessage(), e);
            throw new CustomException(ProblemErrorCode.PROBLEM_FETCH_FAILED);
        }
    }

    // 관리자
    @Transactional
    public Response createProblem(ProblemCreateReq req) {
        Problem problem = req.toEntity();
        Problem savedProblem = problemJpaRepo.save(problem);

        // 테스트 케이스 저장
        if (req.testCases() != null && !req.testCases().isEmpty()) {
            for (ProblemCreateReq.TestCaseReq tcReq : req.testCases()) {
                ProblemTestCase testCase = ProblemTestCase.builder()
                        .problem(savedProblem)
                        .input(tcReq.input())
                        .output(tcReq.output())
                        .build();
                testCaseJpaRepo.save(testCase);
            }
        }

        log.info("문제 생성 완료 - problemId: {}", savedProblem.getProblemId());
        return Response.created("문제가 성공적으로 생성되었습니다.");
    }

    @Transactional
    public Response updateProblem(Long problemId, ProblemUpdateReq req) {
        Problem problem = problemJpaRepo.findById(problemId)
                .orElseThrow(() -> new CustomException(ProblemErrorCode.PROBLEM_NOT_FOUND));

        problem.updateProblem(req.name(), req.description(), req.input(), req.output(), req.difficulty());

        // 기존 테스트 케이스 삭제 후 새로 추가
        testCaseJpaRepo.deleteAll(testCaseJpaRepo.findByProblem_ProblemId(problemId));

        if (req.testCases() != null && !req.testCases().isEmpty()) {
            for (ProblemCreateReq.TestCaseReq tcReq : req.testCases()) {
                ProblemTestCase testCase = ProblemTestCase.builder()
                        .problem(problem)
                        .input(tcReq.input())
                        .output(tcReq.output())
                        .build();
                testCaseJpaRepo.save(testCase);
            }
        }

        return Response.ok("문제가 성공적으로 수정되었습니다.");
    }

    @Transactional
    public Response deleteProblem(Long problemId) {
        Problem problem = problemJpaRepo.findById(problemId)
                .orElseThrow(() -> new CustomException(ProblemErrorCode.PROBLEM_NOT_FOUND));

        testCaseJpaRepo.deleteAll(testCaseJpaRepo.findByProblem_ProblemId(problemId));
        problemJpaRepo.delete(problem);

        return Response.ok("문제가 성공적으로 삭제되었습니다.");
    }
}
