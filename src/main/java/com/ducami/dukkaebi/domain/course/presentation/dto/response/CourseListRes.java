package com.ducami.dukkaebi.domain.course.presentation.dto.response;

import com.ducami.dukkaebi.domain.course.domain.Course;
import com.ducami.dukkaebi.domain.course.domain.enums.LevelType;

import java.util.List;

public record CourseListRes(
        Long courseId,
        String title,
        List<String> keywords,
        LevelType level
) {
    public static CourseListRes from(Course course) {
        return new CourseListRes(
                course.getId(),
                course.getTitle(),
                course.getKeywords(),
                course.getLevel()
        );
    }
}
