package com.ducami.dukkaebi.domain.course.presentation.dto.request;

import com.ducami.dukkaebi.domain.course.domain.Course;
import com.ducami.dukkaebi.domain.course.domain.enums.LevelType;

import java.util.ArrayList;
import java.util.List;

public record CourseReq(
        String title,
        String description,
        List<String> keywords,
        LevelType level
) {
    public static Course fromReq(CourseReq req) {
        return Course.builder()
                .title(req.title)
                .description(req.description)
                .keywords(new ArrayList<>(req.keywords))
                .level(req.level)
                .build();
    }
}
