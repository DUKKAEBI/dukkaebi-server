package com.ducami.dukkaebi.domain.course.service;

import com.ducami.dukkaebi.domain.course.domain.Course;
import com.ducami.dukkaebi.domain.course.domain.enums.CourseStatus;
import com.ducami.dukkaebi.domain.problem.domain.ProblemHistory;
import com.ducami.dukkaebi.domain.problem.domain.enums.SolvedResult;
import com.ducami.dukkaebi.domain.problem.domain.repo.ProblemHistoryJpaRepo;
import com.ducami.dukkaebi.global.security.auth.UserSessionHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseProgressService {
    private final ProblemHistoryJpaRepo problemHistoryJpaRepo;
    private final UserSessionHolder userSessionHolder;

    // 진행 퍼센트 계산: SOLVED 개수 / 전체 문제 수 * 100
    @Transactional(readOnly = true)
    public Integer calculateProgressPercent(Course course) {
        List<Long> problemIds = course.getProblemIds();
        if (problemIds == null || problemIds.isEmpty()) return 0;

        Long userId = userSessionHolder.getUserId();
        List<ProblemHistory> histories = problemHistoryJpaRepo.findByUser_Id(userId);

        Integer solvedCount = 0;
        for (ProblemHistory h : histories) {
            Long pid = h.getProblem().getProblemId();
            if (problemIds.contains(pid) && h.getSolvedResult() == SolvedResult.SOLVED) {
                solvedCount++;
            }
        }
        return (int) Math.round((solvedCount * 100.0) / problemIds.size());
    }

    // 코스 상태 계산: 수강 여부 + 진행도 기반
    @Transactional(readOnly = true)
    public CourseStatus calculateCourseStatus(Course course) {
        Long userId = userSessionHolder.getUserId();

        // 수강 신청 안 함
        if (!course.hasParticipant(userId)) {
            return CourseStatus.NOT_STARTED;
        }

        // 수강 중 - 진행도로 판단
        Integer progress = calculateProgressPercent(course);
        return progress >= 100 ? CourseStatus.COMPLETED : CourseStatus.IN_PROGRESS;
    }
}
