package com.ducami.dukkaebi.domain.course.presentation.dto.response;

import com.ducami.dukkaebi.domain.course.domain.Course;

public record CourseStudentItemRes(
        Long courseId,
        String title,
        int progressPercent
) {
    public static CourseStudentItemRes from(Course course, int progressPercent) {
        return new CourseStudentItemRes(
                course.getId(),
                course.getTitle(),
                progressPercent
        );
    }
}

