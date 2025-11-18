package com.ducami.dukkaebi.domain.grading.service;

public record ExecutionResult(
        String output,
        String error,
        boolean success,
        boolean timeout
) {}