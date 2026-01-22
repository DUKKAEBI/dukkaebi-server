package com.ducami.dukkaebi.domain.grading.presentation.dto.request;

public record CodeSaveReq(
        Long problemId,
        String code,
        String language  // "java", "python", "cpp"
) {}

