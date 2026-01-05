package com.ducami.dukkaebi.domain.contest.presentation.dto.request;

public record ContestScoreUpdateReq(
        Long problemId,
        Integer earnedScore
) {}

