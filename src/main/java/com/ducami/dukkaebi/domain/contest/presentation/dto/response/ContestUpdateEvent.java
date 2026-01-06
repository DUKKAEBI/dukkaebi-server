package com.ducami.dukkaebi.domain.contest.presentation.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ContestUpdateEvent(
        String eventType,
        String contestCode,
        String title,
        String description,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String message
) {
    public static ContestUpdateEvent of(String contestCode, String title, String description,
                                       LocalDateTime startDate, LocalDateTime endDate) {
        return ContestUpdateEvent.builder()
                .eventType("CONTEST_UPDATED")
                .contestCode(contestCode)
                .title(title)
                .description(description)
                .startDate(startDate)
                .endDate(endDate)
                .message("대회 정보가 업데이트되었습니다.")
                .build();
    }
}

