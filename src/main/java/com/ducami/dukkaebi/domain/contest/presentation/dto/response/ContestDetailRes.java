package com.ducami.dukkaebi.domain.contest.presentation.dto.response;

import com.ducami.dukkaebi.domain.contest.domain.Contest;
import com.ducami.dukkaebi.domain.contest.domain.enums.ContestStatus;
import com.ducami.dukkaebi.domain.problem.presentation.dto.response.ProblemRes;
import java.time.LocalDate;
import java.time.ZoneId;
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
    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    public static ContestDetailRes from(Contest contest, List<ProblemRes> problems, Long userId) {
        int participantCount = contest.getParticipantIds() == null ? 0 : contest.getParticipantIds().size();

        // Contest에 status가 명시되어 있으면 우선 사용 (관리자가 강제 종료한 경우)
        ContestStatus status = contest.getStatus();
        if (status != ContestStatus.ENDED) {
            LocalDate today = LocalDate.now(ZONE);
            LocalDate end = contest.getEndDate();

            // 날짜로 종료 여부 판단
            if (end.isBefore(today)) {
                status = ContestStatus.ENDED;
            } else if (contest.getParticipantIds() != null && userId != null && contest.getParticipantIds().contains(userId)) {
                status = ContestStatus.JOINED;
            } else {
                status = ContestStatus.JOINABLE;
            }
        }

        return ContestDetailRes.builder()
                .code(contest.getCode())
                .title(contest.getTitle())
                .description(contest.getDescription())
                .startDate(contest.getStartDate())
                .endDate(contest.getEndDate())
                .status(status)
                .participantCount(participantCount)
                .problems(problems)
                .build();
    }
}
