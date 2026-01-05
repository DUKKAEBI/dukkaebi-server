package com.ducami.dukkaebi.domain.contest.presentation.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record ContestParticipantListRes(
        Integer rank,
        Long userId,
        String nickname,
        Integer totalScore,
        String totalTime, // "HH:MM:SS" 형식
        List<ProblemScoreDetail> problemScores
) {
    @Builder
    public record ProblemScoreDetail(
            Long problemId,
            Integer earnedScore,
            Integer maxScore
    ) {}
}

