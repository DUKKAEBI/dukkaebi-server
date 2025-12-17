package com.ducami.dukkaebi.domain.contest.presentation.dto.response;

import com.ducami.dukkaebi.domain.contest.domain.Contest;
import com.ducami.dukkaebi.domain.contest.domain.enums.ContestStatus;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public record ContestListRes(
        String code,
        String title,
        String dDay,
        int participantCount,
        ContestStatus status
) {
    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    public static ContestListRes from(Contest contest, Long userId) {
        LocalDate today = LocalDate.now(ZONE);
        LocalDate end = contest.getEndDate();
        long diff = ChronoUnit.DAYS.between(today, end);
        String dDayStr;
        if (diff > 0) {
            dDayStr = "종료까지 D-" + diff; // 예: D-2
        } else if (diff == 0) {
            dDayStr = "오늘 종료";
        } else { // 이미 종료
            dDayStr = "종료됨";
        }

        int count = contest.getParticipantIds() == null ? 0 : contest.getParticipantIds().size();

        // Contest에 status가 명시되어 있으면 우선 사용 (관리자가 강제 종료한 경우)
        ContestStatus status = contest.getStatus();
        if (status != ContestStatus.ENDED) {
            // 날짜로 종료 여부 판단
            if (end.isBefore(today)) {
                status = ContestStatus.ENDED;
            } else if (contest.getParticipantIds() != null && userId != null && contest.getParticipantIds().contains(userId)) {
                status = ContestStatus.JOINED;
            } else {
                status = ContestStatus.JOINABLE;
            }
        }

        return new ContestListRes(contest.getCode(), contest.getTitle(), dDayStr, count, status);
    }
}