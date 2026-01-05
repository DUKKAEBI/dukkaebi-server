package com.ducami.dukkaebi.domain.problem.presentation.dto.request;

import com.ducami.dukkaebi.domain.problem.domain.Problem;
import com.ducami.dukkaebi.domain.problem.domain.enums.DifficultyType;

import java.time.LocalDate;
import java.util.List;

public record ProblemCreateReq(
        String name,
        String description,
        String input,
        String output,
        DifficultyType difficulty,
        Integer score,
        List<TestCaseReq> testCases
) {
    public record TestCaseReq(
            String input,
            String output
    ) {}

    // 일반 문제 생성
    public Problem toEntity() {
        return Problem.builder()
                .name(name)
                .description(description)
                .input(input)
                .output(output)
                .difficulty(difficulty)
                .solvedCount(0)
                .attemptCount(0)
                .addedAt(LocalDate.now())
                .build();
    }

    // 대회 전용 문제 생성 (점수 기반)
    public Problem toContestEntity(String contestId) {
        return Problem.builder()
                .name(name)
                .description(description)
                .input(input)
                .output(output)
                .difficulty(null)
                .score(score)
                .solvedCount(0)
                .attemptCount(0)
                .addedAt(LocalDate.now())
                .contestId(contestId)
                .build();
    }
}
