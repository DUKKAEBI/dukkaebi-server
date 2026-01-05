package com.ducami.dukkaebi.domain.course.presentation.controller;

import com.ducami.dukkaebi.domain.contest.presentation.dto.response.ContestListRes;
import com.ducami.dukkaebi.domain.course.presentation.dto.response.CourseDetailRes;
import com.ducami.dukkaebi.domain.course.presentation.dto.response.CourseListRes;
import com.ducami.dukkaebi.domain.course.usecase.CourseUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "코스 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/course")
public class CourseController {
    private final CourseUseCase courseUseCase;

    @GetMapping("/list")
    @Operation(summary = "코스 리스트 조회")
    public List<CourseListRes> getCourseList() {
        return courseUseCase.getCourseList();
    }

    @GetMapping("/{courseId}")
    @Operation(summary = "코스 상세 조회")
    public CourseDetailRes getCourseDetail(@PathVariable("courseId") Long courseId) {
        return courseUseCase.getCourseDetail(courseId);
    }

    @GetMapping("/search")
    @Operation(summary = "이름으로 검색")
    public List<CourseListRes> getCourseWithName(@RequestParam String name) {
        return courseUseCase.getCourseWithName(name);
    }
}
