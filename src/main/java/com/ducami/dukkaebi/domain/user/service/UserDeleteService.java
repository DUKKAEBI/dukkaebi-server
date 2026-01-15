package com.ducami.dukkaebi.domain.user.service;

import com.ducami.dukkaebi.domain.contest.domain.Contest;
import com.ducami.dukkaebi.domain.contest.domain.repo.ContestJpaRepo;
import com.ducami.dukkaebi.domain.course.domain.Course;
import com.ducami.dukkaebi.domain.course.domain.repo.CourseJpaRepo;
import com.ducami.dukkaebi.domain.problem.domain.repo.ProblemHistoryJpaRepo;
import com.ducami.dukkaebi.domain.user.domain.User;
import com.ducami.dukkaebi.domain.user.domain.repo.UserDailyActivityJpaRepo;
import com.ducami.dukkaebi.domain.user.domain.repo.UserJpaRepo;
import com.ducami.dukkaebi.domain.user.error.UserErrorCode;
import com.ducami.dukkaebi.global.common.dto.response.Response;
import com.ducami.dukkaebi.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDeleteService {
    private final UserJpaRepo userJpaRepo;
    private final UserDailyActivityJpaRepo userDailyActivityJpaRepo;
    private final ProblemHistoryJpaRepo problemHistoryJpaRepo;
    private final CourseJpaRepo courseJpaRepo;
    private final ContestJpaRepo contestJpaRepo;

    /**
     * 사용자와 관련된 모든 데이터를 삭제합니다.
     * 1. 사용자의 일일 활동 기록 삭제
     * 2. 사용자의 문제 풀이 기록 삭제
     * 3. 코스 참가자 목록에서 제거
     * 4. 콘테스트 참가자 목록에서 제거
     * 5. 사용자 정보 삭제
     */
    @Transactional
    public Response deleteUserWithRelatedData(Long userId) {
        User user = userJpaRepo.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        log.info("사용자 삭제 시작: userId={}, nickname={}", userId, user.getNickname());

        // 1. 일일 활동 기록 삭제
        int deletedActivities = userDailyActivityJpaRepo.deleteByUser_Id(userId);
        log.info("일일 활동 기록 삭제 완료: {}건", deletedActivities);

        // 2. 문제 풀이 기록 삭제
        int deletedHistories = problemHistoryJpaRepo.deleteByUser_Id(userId);
        log.info("문제 풀이 기록 삭제 완료: {}건", deletedHistories);

        // 3. 코스 참가자 목록에서 제거
        List<Course> courses = courseJpaRepo.findAll();
        int removedFromCourses = 0;
        for (Course course : courses) {
            if (course.hasParticipant(userId)) {
                course.getParticipantIds().remove(userId);
                removedFromCourses++;
            }
        }
        if (removedFromCourses > 0) {
            courseJpaRepo.saveAll(courses);
            log.info("코스 참가자 목록에서 제거 완료: {}개 코스", removedFromCourses);
        }

        // 4. 콘테스트 참가자 목록에서 제거
        List<Contest> contests = contestJpaRepo.findAll();
        int removedFromContests = 0;
        for (Contest contest : contests) {
            if (contest.hasParticipant(userId)) {
                contest.getParticipantIds().remove(userId);
                removedFromContests++;
            }
        }
        if (removedFromContests > 0) {
            contestJpaRepo.saveAll(contests);
            log.info("콘테스트 참가자 목록에서 제거 완료: {}개 콘테스트", removedFromContests);
        }

        // 5. 사용자 삭제
        userJpaRepo.delete(user);
        log.info("사용자 삭제 완료: userId={}", userId);

        return Response.ok("사용자 및 관련 데이터 삭제에 성공하였습니다.");
    }

}
