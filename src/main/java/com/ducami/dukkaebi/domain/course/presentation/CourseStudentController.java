package com.ducami.dukkaebi.domain.course.presentation;

import com.ducami.dukkaebi.domain.course.presentation.dto.response.CourseStudentItemRes;
import com.ducami.dukkaebi.domain.course.usecase.CourseUseCase;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/student/course")
public class CourseStudentController {
    private final CourseUseCase courseUseCase;

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
}
