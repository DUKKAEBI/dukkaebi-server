package com.ducami.dukkaebi.domain.problem.presentation.dto.response;

import com.ducami.dukkaebi.domain.problem.domain.Problem;
import com.ducami.dukkaebi.domain.problem.domain.ProblemTestCase;
import com.ducami.dukkaebi.domain.problem.domain.enums.DifficultyType;
import lombok.Builder;

import java.util.List;

@Builder
public record ProblemDetailRes(
        String name,
        String description,
        String input,
        String output,
        DifficultyType difficulty,
        Integer score,
        String exampleInput,
        String exampleOutput,
        List<TestCaseRes> testCases,
        Boolean isContestOnly  // true: 대회 전용 문제, false: 일반 문제를 가져온 것, null: 일반 문제
) {
    public static ProblemDetailRes from(Problem problem, List<ProblemTestCase> testCases) {

        String exampleInput = "";
        String exampleOutput = "";

        if (testCases != null && !testCases.isEmpty()) {
            ProblemTestCase first = testCases.getFirst();

            exampleInput = normalize(first.getInput());
            exampleOutput = normalize(first.getOutput());
        }

        // 테스트케이스를 DTO로 변환
        List<TestCaseRes> testCaseResList = testCases != null
                ? testCases.stream().map(TestCaseRes::from).toList()
                : List.of();

        return new ProblemDetailRes(
                problem.getName(),
                problem.getDescription(),
                problem.getInput(),
                problem.getOutput(),
                problem.getDifficulty(),
                problem.getScore(),
                exampleInput,
                exampleOutput,
                testCaseResList,
                problem.getContestId() != null  // contestId가 있으면 대회 전용 문제
        );
    }

    private static String normalize(String text) {
        if (text == null) return "";

        // CRLF → LF 변환 (윈도우 줄바꿈 제거)
        text = text.replace("\r\n", "\n");
        // CR → LF 변환 (매킨토시 옛 버전)
        text = text.replace("\r", "\n");

        // 끝에 줄바꿈이 여러 개 있을 경우 최소한 하나만
        return text;
    }
}

