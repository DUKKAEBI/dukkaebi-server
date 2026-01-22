package com.ducami.dukkaebi.domain.grading.presentation.dto.response;

import com.ducami.dukkaebi.domain.grading.domain.SavedCode;

import java.time.LocalDateTime;

public record SavedCodeRes(
        Long problemId,
        String code,
        String language,
        LocalDateTime updatedAt
) {
    public static SavedCodeRes from(SavedCode savedCode) {
        return new SavedCodeRes(
                savedCode.getProblem().getProblemId(),
                savedCode.getCode(),
                savedCode.getLanguage(),
                savedCode.getUpdatedAt()
        );
    }
}

