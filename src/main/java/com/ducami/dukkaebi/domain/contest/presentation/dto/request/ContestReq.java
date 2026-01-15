package com.ducami.dukkaebi.domain.contest.presentation.dto.request;

import com.ducami.dukkaebi.domain.contest.domain.Contest;
import com.ducami.dukkaebi.domain.contest.domain.enums.ContestStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record ContestReq(
        String title,
        String description,
        String imageUrl,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
    public static Contest fromReq(String code, ContestReq req) {
        return Contest.builder()
                .code(code)
                .title(req.title)
                .description(req.description)
                .imageUrl(req.imageUrl)
                .startDate(req.startDate)
                .endDate(req.endDate)
                .status(ContestStatus.JOINABLE)
                .participantIds(new ArrayList<>(List.of()))
                .build();
    }
}