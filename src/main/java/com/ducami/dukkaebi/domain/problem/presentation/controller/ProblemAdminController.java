package com.ducami.dukkaebi.domain.problem.presentation.controller;

import com.ducami.dukkaebi.domain.problem.presentation.dto.request.ProblemCreateReq;
import com.ducami.dukkaebi.domain.problem.presentation.dto.request.ProblemUpdateReq;
import com.ducami.dukkaebi.domain.problem.usecase.ProblemUseCase;
import com.ducami.dukkaebi.global.common.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "문제 관리자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/problems")
public class ProblemAdminController {
    private final ProblemUseCase problemUseCase;

    @PostMapping("/create")
    @Operation(summary = "문제 생성")
    public Response createProblem(@RequestBody ProblemCreateReq req) {
        return problemUseCase.createProblem(req);
    }

    @PutMapping("/update/{problemId}")
    @Operation(summary = "문제 수정")
    public Response updateProblem(@PathVariable("problemId") Long problemId, @RequestBody ProblemUpdateReq req) {
        return problemUseCase.updateProblem(problemId, req);
    }

    @DeleteMapping("delete/{problemId}")
    @Operation(summary = "문제 삭제")
    public Response deleteProblem(@PathVariable("problemId") Long problemId) {
        return problemUseCase.deleteProblem(problemId);
    }
}
