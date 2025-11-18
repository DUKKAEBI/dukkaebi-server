package com.ducami.dukkaebi.domain.problem.presentation.dto.request;

import com.ducami.dukkaebi.domain.problem.domain.enums.DifficultyType;

public record ProblemFilterReq(
        DifficultyType difficulty,    // 난이도 필터
        String time,                 // 시간
        String correctRate           // 정답률
) {}
