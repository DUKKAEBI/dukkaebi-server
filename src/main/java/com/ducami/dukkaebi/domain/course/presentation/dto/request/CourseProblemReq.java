package com.ducami.dukkaebi.domain.course.presentation.dto.request;

import java.util.List;

public record CourseProblemReq(
        List<Long> problemIds
) {
}

