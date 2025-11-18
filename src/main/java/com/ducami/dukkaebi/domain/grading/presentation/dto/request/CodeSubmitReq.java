package com.ducami.dukkaebi.domain.grading.presentation.dto.request;

public record CodeSubmitReq(
        Long problemId,
        String code,
        String language  // "java", "python", "cpp"
) {}