package com.ducami.dukkaebi.domain.problem.presentation.dto.response;

import com.ducami.dukkaebi.domain.problem.domain.Problem;
import com.ducami.dukkaebi.domain.problem.domain.ProblemHistory;
import com.ducami.dukkaebi.domain.problem.domain.enums.DifficultyType;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record ProblemRes (
        Long problemId,
        String name,
        DifficultyType difficulty,
        Integer solvedCount,
        Double correctRate,
        boolean isFailed,
        boolean isPassed,
        LocalDate addedAt
) {
    public static ProblemRes from(Problem problem, ProblemHistory history) {
        return new ProblemRes(
                problem.getProblemId(),
                problem.getName(),
                problem.getDifficulty(),
                problem.getSolvedCount(),
                (problem.getSolvedCount()/problem.getAttemptCount()* 1000.0)/10.0,
                history.getIsFailed(),
                history.getIsSolved(),
                problem.getAddedAt()
        );
    }


}
