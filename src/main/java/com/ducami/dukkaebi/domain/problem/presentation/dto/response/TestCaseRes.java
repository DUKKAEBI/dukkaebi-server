package com.ducami.dukkaebi.domain.problem.presentation.dto.response;

import com.ducami.dukkaebi.domain.problem.domain.ProblemTestCase;
import lombok.Builder;

@Builder
public record TestCaseRes(
        Long id,
        String input,
        String output
) {
    public static TestCaseRes from(ProblemTestCase testCase) {
        return new TestCaseRes(
                testCase.getId(),
                normalize(testCase.getInput()),
                normalize(testCase.getOutput())
        );
    }

    private static String normalize(String text) {
        if (text == null) return "";

        // CRLF → LF 변환 (윈도우 줄바꿈 제거)
        text = text.replace("\r\n", "\n");
        // CR → LF 변환 (매킨토시 옛 버전)
        text = text.replace("\r", "\n");

        return text;
    }
}

