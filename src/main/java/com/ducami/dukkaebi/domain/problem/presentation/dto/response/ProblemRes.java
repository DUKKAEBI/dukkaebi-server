package com.ducami.dukkaebi.domain.problem.presentation.dto.response;

import com.ducami.dukkaebi.domain.problem.domain.Problem;
import com.ducami.dukkaebi.domain.problem.domain.ProblemHistory;
import com.ducami.dukkaebi.domain.problem.domain.enums.DifficultyType;
import com.ducami.dukkaebi.domain.problem.domain.enums.SolvedResult;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record ProblemRes (
        Long problemId,
        String name,
        DifficultyType difficulty,
        Integer score,
        Integer solvedCount,
        Double correctRate,
        SolvedResult solvedResult,
        LocalDate addedAt
) {
    public static ProblemRes from(Problem problem, ProblemHistory history) {
        return new ProblemRes(
                problem.getProblemId(),
                problem.getName(),
                problem.getDifficulty(),
                problem.getScore(),
                problem.getSolvedCount(),
                calculateCorrectRate(problem.getSolvedCount(), problem.getAttemptCount()),                getSolvedResult(history),
                problem.getAddedAt()
        );
    }

    private static SolvedResult getSolvedResult(ProblemHistory problemHistory) {
        return problemHistory != null ? problemHistory.getSolvedResult() : SolvedResult.NOT_SOLVED;
    }

    private static Double calculateCorrectRate(Integer solvedCount, Integer attemptCount) {
        if (solvedCount == null || attemptCount == null || attemptCount == 0) {
            return 0.0;
        }

        double rate = (double) solvedCount / attemptCount * 100.0;
        return Math.round(rate * 10.0) / 10.0;
    }


}
