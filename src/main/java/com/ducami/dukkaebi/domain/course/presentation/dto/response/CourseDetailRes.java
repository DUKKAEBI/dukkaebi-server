package com.ducami.dukkaebi.domain.course.presentation.dto.response;

import com.ducami.dukkaebi.domain.contest.domain.Contest;
import com.ducami.dukkaebi.domain.contest.presentation.dto.response.ContestDetailRes;
import com.ducami.dukkaebi.domain.course.domain.Course;
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
        List<ProblemRes> problems
) {
    public static CourseDetailRes from(Course course, List<ProblemRes> problems) {
        return CourseDetailRes.builder()
                .courseId(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .keywords(course.getKeywords())
                .level(course.getLevel())
                .problems(problems)
                .build();
    }
}
