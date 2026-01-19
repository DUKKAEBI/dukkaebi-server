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
        LocalDate addedAt,
        Boolean isContestOnly  // true: 대회 전용 문제, false: 일반 문제를 가져온 것, null: 일반 문제 목록 조회 시
) {
    public static ProblemRes from(Problem problem, ProblemHistory history) {
        return new ProblemRes(
                problem.getProblemId(),
                problem.getName(),
                problem.getDifficulty(),
                problem.getScore(),
                problem.getSolvedCount(),
                calculateCorrectRate(problem.getSolvedCount(), problem.getAttemptCount()),
                getSolvedResult(history),
                problem.getAddedAt(),
                null  // 일반 문제 목록 조회 시에는 null
        );
    }

    public static ProblemRes from(Problem problem, ProblemHistory history, Integer contestScore) {
        // contestScore가 null이 아니면 일반 문제를 대회에 가져온 것 (false)
        // problem.getContestId()가 null이 아니면 대회 전용 문제 (true)
        Boolean isContestOnly = problem.getContestId() != null ? true : (contestScore != null ? false : null);

        return new ProblemRes(
                problem.getProblemId(),
                problem.getName(),
                problem.getDifficulty(),
                contestScore != null ? contestScore : problem.getScore(),
                problem.getSolvedCount(),
                calculateCorrectRate(problem.getSolvedCount(), problem.getAttemptCount()),
                getSolvedResult(history),
                problem.getAddedAt(),
                isContestOnly
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
