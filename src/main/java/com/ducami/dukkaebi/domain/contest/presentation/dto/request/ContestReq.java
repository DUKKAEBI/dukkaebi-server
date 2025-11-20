package com.ducami.dukkaebi.domain.contest.presentation.dto.request;

import com.ducami.dukkaebi.domain.contest.domain.Contest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public record ContestReq(
        String title,
        String description,
        LocalDate startDate,
        LocalDate endDate
) {
    public static Contest fromReq(String code, ContestReq req) {
        return Contest.builder()
                .code(code)
                .title(req.title)
                .description(req.description)
                .startDate(req.startDate)
                .endDate(req.endDate)
                .participantIds(new ArrayList<>(List.of()))
                .build();
    }
}