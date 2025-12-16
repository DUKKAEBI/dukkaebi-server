package com.ducami.dukkaebi.domain.course.presentation.controller;

import com.ducami.dukkaebi.domain.course.presentation.dto.response.CourseStudentItemRes;
import com.ducami.dukkaebi.domain.course.usecase.CourseUseCase;
import com.ducami.dukkaebi.global.common.Response;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/student/course")
public class CourseStudentController {
    private final CourseUseCase courseUseCase;

    @PostMapping("/{courseId}/join")
    public @ResponseBody Response joinCourse(@PathVariable("courseId") Long courseId) {
        return courseUseCase.joinCourse(courseId);
    }

    @GetMapping("/in-progress")
    @Operation(summary = "진행 중인 코스 조회")
    public @ResponseBody List<CourseStudentItemRes> getInProgress() {
        return courseUseCase.getInProgressCourses();
    }

    @GetMapping("/completed")
    @Operation(summary = "완료된 코스 조회")
    public @ResponseBody List<CourseStudentItemRes> getCompleted() {
        return courseUseCase.getCompletedCourses();
    }

    @GetMapping("/joinable")
    @Operation(summary = "수강 가능 코스 목록 조회")
    public @ResponseBody List<CourseStudentItemRes> getJoinable() {
        return courseUseCase.getJoinableCourses();
    }
}
