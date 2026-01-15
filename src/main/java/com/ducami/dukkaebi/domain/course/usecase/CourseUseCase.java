package com.ducami.dukkaebi.domain.course.usecase;

import com.ducami.dukkaebi.domain.course.domain.Course;
import com.ducami.dukkaebi.domain.course.domain.repo.CourseJpaRepo;
import com.ducami.dukkaebi.domain.course.error.CourseErrorCode;
import com.ducami.dukkaebi.domain.course.presentation.dto.request.CourseProblemReq;
import com.ducami.dukkaebi.domain.course.presentation.dto.request.CourseReq;
import com.ducami.dukkaebi.domain.course.presentation.dto.response.CourseDetailRes;
import com.ducami.dukkaebi.domain.course.presentation.dto.response.CourseListRes;
import com.ducami.dukkaebi.domain.course.presentation.dto.response.CourseListWithCountRes;
import com.ducami.dukkaebi.domain.course.presentation.dto.response.CourseStudentItemRes;
import com.ducami.dukkaebi.domain.course.service.CourseProgressService;
import com.ducami.dukkaebi.domain.problem.domain.Problem;
import com.ducami.dukkaebi.domain.problem.domain.ProblemHistory;
import com.ducami.dukkaebi.domain.problem.domain.repo.ProblemHistoryJpaRepo;
import com.ducami.dukkaebi.domain.problem.domain.repo.ProblemJpaRepo;
import com.ducami.dukkaebi.domain.problem.error.ProblemErrorCode;
import com.ducami.dukkaebi.domain.problem.presentation.dto.response.ProblemRes;
import com.ducami.dukkaebi.global.common.PageResponse;
import com.ducami.dukkaebi.global.common.Response;
import com.ducami.dukkaebi.global.exception.CustomException;
import com.ducami.dukkaebi.domain.course.domain.enums.CourseStatus;
import com.ducami.dukkaebi.global.security.auth.UserSessionHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseUseCase {
    private final CourseJpaRepo courseJpaRepo;
    private final ProblemJpaRepo problemJpaRepo;
    private final ProblemHistoryJpaRepo problemHistoryJpaRepo;
    private final CourseProgressService courseProgressService;
    private final UserSessionHolder userSessionHolder;

    @Transactional(readOnly = true)
    public PageResponse<CourseListRes> getCourseListPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CourseListRes> coursePage = courseJpaRepo.findAll(pageable)
                .map(CourseListRes::from);
        return PageResponse.of(coursePage);
    }

    @Transactional(readOnly = true)
    public PageResponse<CourseListRes> getCourseWithNamePaged(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CourseListRes> coursePage = courseJpaRepo.findByTitleContainingIgnoreCase(name, pageable)
                .map(CourseListRes::from);
        return PageResponse.of(coursePage);
    }

    @Transactional(readOnly = true)
    public CourseDetailRes getCourseDetail(Long courseId) {
        Course course = courseJpaRepo.findById(courseId)
                .orElseThrow(() -> new CustomException(CourseErrorCode.COURSE_NOT_FOUND));

        // 현재 사용자 ID 조회
        Long userId = null;
        try { userId = userSessionHolder.getUserId(); } catch (Exception ignored) {}
        final Long finalUserId = userId;

        List<Long> problemIds = course.getProblemIds();
        List<ProblemRes> problems = problemIds == null || problemIds.isEmpty()
                ? List.of()
                : problemJpaRepo.findAllById(problemIds).stream()
                .map((Problem p) -> {
                    ProblemHistory history = null;
                    if (finalUserId != null) {
                        history = problemHistoryJpaRepo.findByUser_IdAndProblem_ProblemId(finalUserId, p.getProblemId()).orElse(null);
                    }
                    return ProblemRes.from(p, history);
                })
                .toList();

        // 수강 여부, 진행도, 상태 계산
        boolean isEnrolled = userId != null && course.hasParticipant(userId);
        Integer progressPercent = courseProgressService.calculateProgressPercent(course);
        CourseStatus status = courseProgressService.calculateCourseStatus(course);

        return CourseDetailRes.from(course, problems, isEnrolled, progressPercent, status);
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

    // 학생: 코스 수강 신청
    @Transactional
    public Response joinCourse(Long courseId) {
        Course course = courseJpaRepo.findById(courseId)
                .orElseThrow(() -> new CustomException(CourseErrorCode.COURSE_NOT_FOUND));

        Long userId = userSessionHolder.getUserId();
        course.addParticipant(userId);

        return Response.ok("코스 수강 신청이 완료되었습니다.");
    }

    // 학생: 진행 중 코스 목록 (IN_PROGRESS)
    @Transactional(readOnly = true)
    public List<CourseStudentItemRes> getInProgressCourses() {
        return courseJpaRepo.findAll().stream()
                .map(c -> {
                    Integer progress = courseProgressService.calculateProgressPercent(c);
                    CourseStatus status = courseProgressService.calculateCourseStatus(c);
                    return CourseStudentItemRes.from(c, progress, status);
                })
                .filter(item -> item.status() == CourseStatus.IN_PROGRESS)
                .toList();
    }

    // 학생: 완료 코스 목록 (COMPLETED)
    @Transactional(readOnly = true)
    public CourseListWithCountRes getCompletedCourses() {
        List<CourseStudentItemRes> allCourses = courseJpaRepo.findAll().stream()
                .map(c -> {
                    Integer progress = courseProgressService.calculateProgressPercent(c);
                    CourseStatus status = courseProgressService.calculateCourseStatus(c);
                    return CourseStudentItemRes.from(c, progress, status);
                })
                .toList();

        // 완료된 코스 필터링
        List<CourseStudentItemRes> completedCourses = allCourses.stream()
                .filter(item -> item.status() == CourseStatus.COMPLETED)
                .toList();

        // 진행 중인 코스 개수 계산
        Integer inProgressCount = (int) allCourses.stream()
                .filter(item -> item.status() == CourseStatus.IN_PROGRESS)
                .count();

        return CourseListWithCountRes.of(inProgressCount, completedCourses);
    }

    // 학생: 수강 가능한 코스 목록 (NOT_STARTED)
    @Transactional(readOnly = true)
    public List<CourseStudentItemRes> getJoinableCourses() {
        return courseJpaRepo.findAll().stream()
                .map(c -> {
                    Integer progress = courseProgressService.calculateProgressPercent(c);
                    CourseStatus status = courseProgressService.calculateCourseStatus(c);
                    return CourseStudentItemRes.from(c, progress, status);
                })
                .filter(item -> item.status() == CourseStatus.NOT_STARTED)
                .toList();
    }

    // 관리자: 코스에 문제 추가
    @Transactional
    public Response addProblemsToCourse(Long courseId, CourseProblemReq req) {
        Course course = courseJpaRepo.findById(courseId)
                .orElseThrow(() -> new CustomException(CourseErrorCode.COURSE_NOT_FOUND));

        if (req.problemIds() == null || req.problemIds().isEmpty()) {
            return Response.ok("추가할 문제가 없습니다.");
        }

        // 문제들 존재 여부 및 대회 전용 문제 확인
        List<Problem> problems = problemJpaRepo.findAllById(req.problemIds());

        if (problems.size() != req.problemIds().size()) {
            throw new CustomException(ProblemErrorCode.PROBLEM_NOT_FOUND);
        }

        for (Problem problem : problems) {
            if (problem.getContestId() != null) {
                throw new CustomException(ProblemErrorCode.CONTEST_PROBLEM_NOT_ALLOWED);
            }
        }

        List<Long> problemIds = course.getProblemIds();
        if (problemIds == null) {
            problemIds = new ArrayList<>();
        }

        int addedCount = 0;
        for (Long problemId : req.problemIds()) {
            if (!problemIds.contains(problemId)) {
                problemIds.add(problemId);
                addedCount++;
            }
        }

        if (addedCount == 0) {
            throw new CustomException(CourseErrorCode.PROBLEM_ALREADY_IN_COURSE);
        }

        return Response.ok(addedCount + "개의 문제가 코스에 추가되었습니다.");
    }

    // 관리자: 코스에서 문제 제거
    @Transactional
    public Response removeProblemFromCourse(Long courseId, Long problemId) {
        Course course = courseJpaRepo.findById(courseId)
                .orElseThrow(() -> new CustomException(CourseErrorCode.COURSE_NOT_FOUND));

        List<Long> problemIds = course.getProblemIds();
        if (problemIds == null || !problemIds.contains(problemId)) {
            throw new CustomException(CourseErrorCode.PROBLEM_NOT_IN_COURSE);
        }

        problemIds.remove(problemId);
        return Response.ok("문제가 코스에서 제거되었습니다.");
    }
}
