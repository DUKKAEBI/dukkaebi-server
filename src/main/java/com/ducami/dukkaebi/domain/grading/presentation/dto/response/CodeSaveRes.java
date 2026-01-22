package com.ducami.dukkaebi.domain.grading.presentation.dto.response;

import com.ducami.dukkaebi.domain.grading.domain.SavedCode;

import java.time.LocalDateTime;

public record CodeSaveRes(
        Long problemId,
        String code,
        String language,
        LocalDateTime savedAt,
        LocalDateTime updatedAt,
        boolean isNewSave  // 새로 저장된 것인지, 업데이트된 것인지
) {
    public static CodeSaveRes from(SavedCode savedCode, boolean isNewSave) {
        return new CodeSaveRes(
                savedCode.getProblem().getProblemId(),
                savedCode.getCode(),
                savedCode.getLanguage(),
                savedCode.getSavedAt(),
                savedCode.getUpdatedAt(),
                isNewSave
        );
    }
}

