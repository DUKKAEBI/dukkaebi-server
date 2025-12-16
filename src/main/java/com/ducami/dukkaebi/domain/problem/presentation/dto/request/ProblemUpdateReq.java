package com.ducami.dukkaebi.domain.problem.presentation.dto.request;

import com.ducami.dukkaebi.domain.problem.domain.enums.DifficultyType;

import java.util.List;

public record ProblemUpdateReq(
        String name,
        String description,
        String input,
        String output,
        DifficultyType difficulty,
        List<ProblemCreateReq.TestCaseReq> testCases
) {
}

