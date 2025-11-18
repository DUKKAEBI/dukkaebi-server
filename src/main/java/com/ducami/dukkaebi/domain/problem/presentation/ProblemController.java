package com.ducami.dukkaebi.domain.problem.presentation;

import com.ducami.dukkaebi.domain.problem.presentation.dto.response.ProblemRes;
import com.ducami.dukkaebi.domain.problem.usecase.ProblemUseCase;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
