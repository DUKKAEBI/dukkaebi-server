package com.ducami.dukkaebi.domain.course.presentation.dto.response;

import com.ducami.dukkaebi.domain.course.domain.Course;
import com.ducami.dukkaebi.domain.course.domain.enums.CourseStatus;
import com.ducami.dukkaebi.domain.course.domain.enums.LevelType;
import com.ducami.dukkaebi.domain.problem.presentation.dto.response.ProblemRes;
import lombok.Builder;

import java.util.List;

@Builder
public record CourseDetailRes(
        Long courseId,
        String title,
        String description,
        List<String> keywords,
        LevelType level,
        List<ProblemRes> problems,
        boolean isEnrolled,        // 수강 여부
        int progressPercent,       // 진행도 (0~100)
        CourseStatus status        // 코스 상태
) {
    public static CourseDetailRes from(
            Course course,
            List<ProblemRes> problems,
            boolean isEnrolled,
            int progressPercent,
            CourseStatus status
    ) {
        return CourseDetailRes.builder()
                .courseId(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .keywords(course.getKeywords())
                .level(course.getLevel())
                .problems(problems)
                .isEnrolled(isEnrolled)
                .progressPercent(progressPercent)
                .status(status)
                .build();
    }
}