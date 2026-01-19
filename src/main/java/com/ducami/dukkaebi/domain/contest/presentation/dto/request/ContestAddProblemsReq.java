package com.ducami.dukkaebi.domain.contest.presentation.dto.request;

import java.util.List;

public record ContestAddProblemsReq(
        List<Long> problemIds
) {
}

