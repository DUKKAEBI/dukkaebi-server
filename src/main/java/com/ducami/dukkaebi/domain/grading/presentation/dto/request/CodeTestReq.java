package com.ducami.dukkaebi.domain.grading.presentation.dto.request;

public record CodeTestReq(
        Long problemId,
        String code,
        String language  // "java", "python"
) {}

