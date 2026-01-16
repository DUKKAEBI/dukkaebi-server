package com.ducami.dukkaebi.domain.contest.presentation.dto.response;

import com.ducami.dukkaebi.domain.contest.domain.ContestSubmission;
import com.ducami.dukkaebi.domain.problem.domain.Problem;
import com.ducami.dukkaebi.domain.problem.domain.ProblemTestCase;

import java.time.LocalDateTime;
import java.util.List;

public record ContestSubmissionRes(
        // 문제 정보
        String problemName,
        String problemDescription,
        String problemInput,
        String problemOutput,
        String exampleInput,
        String exampleOutput,
        // 제출 정보
        String submittedCode,
        String language,
        LocalDateTime submittedAt
) {
    public static ContestSubmissionRes from(ContestSubmission submission, List<ProblemTestCase> testCases) {
        Problem problem = submission.getProblem();

        String exampleInput = "";
        String exampleOutput = "";

        if (testCases != null && !testCases.isEmpty()) {
            ProblemTestCase first = testCases.get(0);
            exampleInput = normalize(first.getInput());
            exampleOutput = normalize(first.getOutput());
        }

        return new ContestSubmissionRes(
                problem.getName(),
                problem.getDescription(),
                problem.getInput(),
                problem.getOutput(),
                exampleInput,
                exampleOutput,
                submission.getCode(),
                submission.getLanguage(),
                submission.getSubmittedAt()
        );
    }

    private static String normalize(String text) {
        if (text == null) return "";
        text = text.replace("\r\n", "\n");
        text = text.replace("\r", "\n");
        return text;
    }
}

