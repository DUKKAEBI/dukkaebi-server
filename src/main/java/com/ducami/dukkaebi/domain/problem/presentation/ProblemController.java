package com.ducami.dukkaebi.domain.problem.presentation;

import com.ducami.dukkaebi.domain.problem.presentation.dto.request.ProblemFilterReq;
import com.ducami.dukkaebi.domain.problem.presentation.dto.response.ProblemDetailRes;
import com.ducami.dukkaebi.domain.problem.presentation.dto.response.ProblemRes;
import com.ducami.dukkaebi.domain.problem.usecase.ProblemUseCase;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/problems")
public class ProblemController {
    private final ProblemUseCase problemUseCase;

    @GetMapping
    @Operation(summary = "모든 문제 조회")
    public List<ProblemRes> getProblems() {
        return problemUseCase.getProblem();
    }

    @GetMapping("/filter")
    @Operation(summary = "문제 필터링")
    public List<ProblemRes> getProblems(@RequestBody ProblemFilterReq filter) {
        return problemUseCase.getProblemWithFilter(filter);
    }

    @GetMapping("/search")
    @Operation(summary = "이름으로 검색")
    public List<ProblemRes> getProblems(@RequestParam String name) {
        return problemUseCase.getProblemWithName(name);
    }

    @GetMapping("/{problemId}")
    @Operation(summary = "문제 자세히 보기")
    public ProblemDetailRes getProblem(@PathVariable Long problemId) {
        return problemUseCase.getOneProblem(problemId);
    }
}
