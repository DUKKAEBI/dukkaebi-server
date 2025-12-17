package com.ducami.dukkaebi.domain.course.presentation.dto.response;

import com.ducami.dukkaebi.domain.course.domain.Course;
import com.ducami.dukkaebi.domain.course.domain.enums.CourseStatus;
import com.ducami.dukkaebi.domain.course.domain.enums.LevelType;

public record CourseStudentItemRes(
        Long courseId,
        String title,
        LevelType level,
        int progressPercent,
        CourseStatus status
) {
    public static CourseStudentItemRes from(Course course, int progressPercent, CourseStatus status) {
        return new CourseStudentItemRes(
                course.getId(),
                course.getTitle(),
                course.getLevel(),
                progressPercent,
                status
        );
    }
}
