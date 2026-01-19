package com.ducami.dukkaebi.domain.problem.presentation.dto.request;

import com.ducami.dukkaebi.domain.problem.domain.enums.DifficultyType;

import java.util.List;

public record ProblemUpdateReq(
        String name,
        String description,
        String input,
        String output,
        DifficultyType difficulty,
        Integer score,  // 대회 문제용 점수 (일반 문제는 null)
        List<ProblemCreateReq.TestCaseReq> testCases
) {
}

