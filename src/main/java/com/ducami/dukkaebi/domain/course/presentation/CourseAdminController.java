package com.ducami.dukkaebi.domain.course.presentation;

import com.ducami.dukkaebi.domain.course.presentation.dto.request.CourseReq;
import com.ducami.dukkaebi.domain.course.usecase.CourseUseCase;
import com.ducami.dukkaebi.global.common.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/course")
public class CourseAdminController {
    private final CourseUseCase courseUseCase;

    @PostMapping("/create")
    public Response createCourse(@RequestBody CourseReq req) {
        return courseUseCase.createCourse(req);
    }

    @PatchMapping("/update/{courseId}")
    public Response updateCourse(@PathVariable("courseId") Long courseId, @RequestBody CourseReq req) {
        return courseUseCase.updateCourse(courseId, req);
    }

    @DeleteMapping("/delete/{courseId}")
    public Response deleteCourse(@PathVariable("courseId") Long courseId) {
        return courseUseCase.deleteCourse(courseId);
    }
}
