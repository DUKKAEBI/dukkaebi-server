package com.ducami.dukkaebi.domain.course.presentation.dto.response;

import java.util.List;

public record CourseListWithCountRes(
        int inProgressCount,
        List<CourseStudentItemRes> courses
) {
    public static CourseListWithCountRes of(int inProgressCount, List<CourseStudentItemRes> courses) {
        return new CourseListWithCountRes(inProgressCount, courses);
    }
}

