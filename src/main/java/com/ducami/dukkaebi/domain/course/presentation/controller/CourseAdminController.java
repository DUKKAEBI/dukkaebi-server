package com.ducami.dukkaebi.domain.course.presentation.controller;

import com.ducami.dukkaebi.domain.course.presentation.dto.request.CourseProblemReq;
import com.ducami.dukkaebi.domain.course.presentation.dto.request.CourseReq;
import com.ducami.dukkaebi.domain.course.usecase.CourseUseCase;
import com.ducami.dukkaebi.global.common.dto.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "코스 관리자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/course")
public class CourseAdminController {
    private final CourseUseCase courseUseCase;

    @PostMapping("/create")
    @Operation(summary = "코스 생성")
    public Response createCourse(@RequestBody CourseReq req) {
        return courseUseCase.createCourse(req);
    }

    @PatchMapping("/update/{courseId}")
    @Operation(summary = "코스 수정")
    public Response updateCourse(@PathVariable("courseId") Long courseId, @RequestBody CourseReq req) {
        return courseUseCase.updateCourse(courseId, req);
    }

    @DeleteMapping("/delete/{courseId}")
    @Operation(summary = "코스 삭제")
    public Response deleteCourse(@PathVariable("courseId") Long courseId) {
        return courseUseCase.deleteCourse(courseId);
    }

    @PostMapping("/{courseId}/problems")
    @Operation(summary = "코스에 문제 추가")
    public Response addProblemsToCourse(@PathVariable("courseId") Long courseId, @RequestBody CourseProblemReq req) {
        return courseUseCase.addProblemsToCourse(courseId, req);
    }

    @DeleteMapping("/{courseId}/problem/{problemId}")
    @Operation(summary = "코스에서 문제 삭제")
    public Response removeProblemFromCourse(@PathVariable("courseId") Long courseId, @PathVariable("problemId") Long problemId) {
        return courseUseCase.removeProblemFromCourse(courseId, problemId);
    }
}
