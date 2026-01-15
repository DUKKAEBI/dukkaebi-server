package com.ducami.dukkaebi.domain.contest.presentation.dto.response;

import com.ducami.dukkaebi.domain.contest.domain.Contest;
import com.ducami.dukkaebi.domain.contest.domain.enums.ContestStatus;
import com.ducami.dukkaebi.domain.problem.presentation.dto.response.ProblemRes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.Builder;

@Builder
public record ContestDetailRes(
        String code,
        String title,
        String description,
        String imageUrl,
        LocalDateTime startDate,
        LocalDateTime endDate,
        ContestStatus status,
        Integer participantCount,
        List<ProblemRes> problems
) {
    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    public static ContestDetailRes from(Contest contest, List<ProblemRes> problems, Long userId) {
        Integer participantCount = contest.getParticipantIds() == null ? 0 : contest.getParticipantIds().size();

        // Contest에 status가 명시되어 있으면 우선 사용 (관리자가 강제 종료한 경우)
        ContestStatus status = contest.getStatus();
        if (status != ContestStatus.ENDED) {
            LocalDateTime now = LocalDateTime.now(ZONE);
            LocalDateTime end = contest.getEndDate();

            // 날짜로 종료 여부 판단
            if (end != null && end.isBefore(now)) {
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
                .imageUrl(contest.getImageUrl())
                .startDate(contest.getStartDate())
                .endDate(contest.getEndDate())
                .status(status)
                .participantCount(participantCount)
                .problems(problems)
                .build();
    }
}
