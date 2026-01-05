package com.ducami.dukkaebi.domain.grading.presentation.dto.request;

public record CodeSubmitReq(
        Long problemId,
        String code,
        String language,  // "java", "python"
        Integer timeSpentSeconds  // 해당 문제에 소요한 시간 (초), 대회 문제에만 해당
) {}