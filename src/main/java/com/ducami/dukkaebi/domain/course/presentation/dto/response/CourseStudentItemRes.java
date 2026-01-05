package com.ducami.dukkaebi.domain.course.presentation.dto.response;

import com.ducami.dukkaebi.domain.course.domain.Course;
import com.ducami.dukkaebi.domain.course.domain.enums.CourseStatus;
import com.ducami.dukkaebi.domain.course.domain.enums.LevelType;

import java.util.List;

public record CourseStudentItemRes(
        Long courseId,
        String title,
        LevelType level,
        List<String> keywords,
        Integer progressPercent,
        CourseStatus status
) {
    public static CourseStudentItemRes from(Course course, Integer progressPercent, CourseStatus status) {
        return new CourseStudentItemRes(
                course.getId(),
                course.getTitle(),
                course.getLevel(),
                course.getKeywords(),
                progressPercent,
                status
        );
    }
}
