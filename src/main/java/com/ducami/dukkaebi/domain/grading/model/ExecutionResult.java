package com.ducami.dukkaebi.domain.grading.model;

public record ExecutionResult(
        String output,
        String error,
        boolean success,
        boolean timeout
) {}