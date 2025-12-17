package com.ducami.dukkaebi.domain.contest.presentation.dto.response;

import com.ducami.dukkaebi.domain.contest.domain.Contest;
import com.ducami.dukkaebi.domain.contest.domain.enums.ContestStatus;
import com.ducami.dukkaebi.domain.problem.presentation.dto.response.ProblemRes;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;

@Builder
public record ContestDetailRes(
        String code,
        String title,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        ContestStatus status,
        int participantCount,
        List<ProblemRes> problems
) {
    public static ContestDetailRes from(Contest contest, List<ProblemRes> problems) {
        int participantCount = contest.getParticipantIds() == null ? 0 : contest.getParticipantIds().size();
        return ContestDetailRes.builder()
                .code(contest.getCode())
                .title(contest.getTitle())
                .description(contest.getDescription())
                .startDate(contest.getStartDate())
                .endDate(contest.getEndDate())
                .status(contest.getStatus())
                .participantCount(participantCount)
                .problems(problems)
                .build();
    }
}
