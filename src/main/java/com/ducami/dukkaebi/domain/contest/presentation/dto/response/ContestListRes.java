package com.ducami.dukkaebi.domain.contest.presentation.dto.response;

import com.ducami.dukkaebi.domain.contest.domain.Contest;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public record ContestListRes(
        String code,
        String title,
        String dDay
) {
    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    public static ContestListRes from(Contest contest) {
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
        return new ContestListRes(contest.getCode(), contest.getTitle(), dDayStr);
    }
}
