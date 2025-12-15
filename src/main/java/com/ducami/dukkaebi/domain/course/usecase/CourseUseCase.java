package com.ducami.dukkaebi.domain.course.usecase;

import com.ducami.dukkaebi.domain.course.domain.Course;
import com.ducami.dukkaebi.domain.course.domain.repo.CourseJpaRepo;
import com.ducami.dukkaebi.domain.course.error.CourseErrorCode;
import com.ducami.dukkaebi.domain.course.presentation.dto.request.CourseReq;
import com.ducami.dukkaebi.domain.course.presentation.dto.response.CourseDetailRes;
import com.ducami.dukkaebi.domain.course.presentation.dto.response.CourseListRes;
import com.ducami.dukkaebi.domain.problem.domain.Problem;
import com.ducami.dukkaebi.domain.problem.domain.repo.ProblemJpaRepo;
import com.ducami.dukkaebi.domain.problem.presentation.dto.response.ProblemRes;
import com.ducami.dukkaebi.global.common.Response;
import com.ducami.dukkaebi.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseUseCase {
    private final CourseJpaRepo courseJpaRepo;
    private final ProblemJpaRepo problemJpaRepo;

    @Transactional(readOnly = true)
    public List<CourseListRes> getCourseList() {
        return courseJpaRepo.findAll().stream()
                .map(CourseListRes::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CourseDetailRes getCourseDetail(Long courseId) {
        Course course = courseJpaRepo.findById(courseId)
                .orElseThrow(() -> new CustomException(CourseErrorCode.COURSE_NOT_FOUND));

        List<Long> problemIds = course.getProblemIds();
        List<ProblemRes> problems = problemIds == null || problemIds.isEmpty()
                ? List.of()
                : problemJpaRepo.findAllById(problemIds).stream()
                .map((Problem p) -> ProblemRes.from(p, null))
                .toList();

        return CourseDetailRes.from(course, problems);
    }

    @Transactional
    public Response createCourse(CourseReq req) {
        if (courseJpaRepo.existsByTitle(req.title())) {
            throw new CustomException(CourseErrorCode.TITLE_ALREADY);
        }

        courseJpaRepo.save(CourseReq.fromReq(req));

        return Response.created("코스가 성공적으로 생성되었습니다.");
    }

    @Transactional
    public Response updateCourse(Long courseId, CourseReq req) {
        Course course = courseJpaRepo.findById(courseId)
                .orElseThrow(() -> new CustomException(CourseErrorCode.COURSE_NOT_FOUND));

        if (!course.getTitle().equals(req.title()) && courseJpaRepo.existsByTitle(req.title())) {
            throw new CustomException(CourseErrorCode.TITLE_ALREADY);
        }

        course.updateCourse(req.title(), req.description(), req.keywords(), req.level());

        return Response.ok("코스가 성공적으로 수정되었습니다.");
    }

    @Transactional
    public Response deleteCourse(Long courseId) {
        Course course = courseJpaRepo.findById(courseId)
                .orElseThrow(() -> new CustomException(CourseErrorCode.COURSE_NOT_FOUND));

        courseJpaRepo.delete(course);

        return Response.ok("코스가 성공적으로 삭제되었습니다.");
    }
}
