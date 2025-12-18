package com.ducami.dukkaebi.domain.grading.presentation.dto.response;

import com.ducami.dukkaebi.domain.grading.domain.enums.JudgeStatus;

import java.util.List;

public record JudgeResultRes(
        JudgeStatus status,        // ACCEPTED, WRONG_ANSWER, RUNTIME_ERROR, TIME_LIMIT_EXCEEDED
        Integer passedTestCases,   // 통과한 테스트케이스 수
        Integer totalTestCases,    // 전체 테스트케이스 수
        Long executionTime,        // 실행 시간 (ms)
        String errorMessage,       // 에러 메시지 (있는 경우)
        List<TestCaseResult> details  // 각 테스트케이스별 결과
) {
    public record TestCaseResult(
            Integer testCaseNumber,
            Boolean passed,
            String input,              // 입력값
            String expectedOutput,     // 예상 출력
            String actualOutput        // 실제 출력
    ) {}
}
