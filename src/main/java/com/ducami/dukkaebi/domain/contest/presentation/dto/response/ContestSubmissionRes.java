package com.ducami.dukkaebi.domain.contest.presentation.dto.response;

import com.ducami.dukkaebi.domain.contest.domain.ContestSubmission;

import java.time.LocalDateTime;

public record ContestSubmissionRes(
        Long submissionId,
        Long problemId,
        String problemTitle,
        Long userId,
        String userNickname,
        String code,
        String language,
        LocalDateTime submittedAt
) {
    public static ContestSubmissionRes from(ContestSubmission submission) {
        return new ContestSubmissionRes(
                submission.getId(),
                submission.getProblem().getProblemId(),
                submission.getProblem().getName(),
                submission.getUser().getId(),
                submission.getUser().getNickname(),
                submission.getCode(),
                submission.getLanguage(),
                submission.getSubmittedAt()
        );
    }
}

