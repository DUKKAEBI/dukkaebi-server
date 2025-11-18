package com.ducami.dukkaebi.domain.grading.domain.enums;

public enum JudgeStatus {
    ACCEPTED("정답"),
    WRONG_ANSWER("오답"),
    RUNTIME_ERROR("런타임 에러"),
    TIME_LIMIT_EXCEEDED("시간 초과"),
    COMPILATION_ERROR("컴파일 에러"),
    MEMORY_LIMIT_EXCEEDED("메모리 초과");

    private final String description;

    JudgeStatus(String description) {
        this.description = description;
    }
}
