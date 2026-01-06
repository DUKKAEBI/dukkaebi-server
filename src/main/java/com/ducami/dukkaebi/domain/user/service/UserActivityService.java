package com.ducami.dukkaebi.domain.user.service;

import com.ducami.dukkaebi.domain.user.domain.UserDailyActivity;
import com.ducami.dukkaebi.domain.user.domain.repo.UserDailyActivityJpaRepo;
import com.ducami.dukkaebi.global.security.auth.UserSessionHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActivityService {
    private final UserDailyActivityJpaRepo activityRepo;
    private final UserSessionHolder userSessionHolder;

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    @Transactional
    public void increaseTodaySolvedCount(Integer delta) {
        Long userId = userSessionHolder.getUserId();
        LocalDate today = LocalDate.now(ZONE);

        UserDailyActivity activity = activityRepo.findByUser_IdAndActivityDate(userId, today)
                .orElseGet(() -> UserDailyActivity.builder()
                        .user(userSessionHolder.getUser())
                        .activityDate(today)
                        .solvedCount(0)
                        .build());

        activity.increaseSolvedCount(delta);
        activityRepo.save(activity);
        log.info("일일 활동 업데이트 - userId: {}, date: {}, solvedCount: {}", userId, today, activity.getSolvedCount());
    }

    @Transactional(readOnly = true)
    public Map<LocalDate, Integer> getContributions(LocalDate start, LocalDate end) {
        Long userId = userSessionHolder.getUserId();
        if (end.isBefore(start)) {
            LocalDate tmp = start; start = end; end = tmp;
        }
        // 기본 0으로 채운 날짜 맵
        Map<LocalDate, Integer> map = new LinkedHashMap<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            map.put(d, 0);
        }
        activityRepo.findAllByUser_IdAndActivityDateBetweenOrderByActivityDate(userId, start, end)
                .forEach(a -> map.put(a.getActivityDate(), a.getSolvedCount()));
        return map;
    }

    @Transactional(readOnly = true)
    public Integer getCurrentStreak() {
        Long userId = userSessionHolder.getUserId();
        LocalDate today = LocalDate.now(ZONE);
        LocalDate start = today.minusDays(365); // 1년치만 조회(성능)
        var list = activityRepo.findAllByUser_IdAndActivityDateBetweenOrderByActivityDate(userId, start, today);

        // 활동 일자를 Set으로 변환
        Set<LocalDate> activeDays = new HashSet<>();
        list.forEach(a -> { if (a.getSolvedCount() > 0) activeDays.add(a.getActivityDate()); });

        Integer streak = 0;
        LocalDate cursor = today;
        while (activeDays.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    // 특정 사용자의 잔디 데이터 조회
    @Transactional(readOnly = true)
    public Map<LocalDate, Integer> getContributionsByUserId(Long userId, LocalDate start, LocalDate end) {
        if (end.isBefore(start)) {
            LocalDate tmp = start; start = end; end = tmp;
        }
        // 기본 0으로 채운 날짜 맵
        Map<LocalDate, Integer> map = new LinkedHashMap<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            map.put(d, 0);
        }
        activityRepo.findAllByUser_IdAndActivityDateBetweenOrderByActivityDate(userId, start, end)
                .forEach(a -> map.put(a.getActivityDate(), a.getSolvedCount()));
        return map;
    }

    // 특정 사용자의 연속 학습일 조회
    @Transactional(readOnly = true)
    public Integer getCurrentStreakByUserId(Long userId) {
        LocalDate today = LocalDate.now(ZONE);
        LocalDate start = today.minusDays(365); // 1년치만 조회(성능)
        var list = activityRepo.findAllByUser_IdAndActivityDateBetweenOrderByActivityDate(userId, start, today);

        // 활동 일자를 Set으로 변환
        Set<LocalDate> activeDays = new HashSet<>();
        list.forEach(a -> { if (a.getSolvedCount() > 0) activeDays.add(a.getActivityDate()); });

        Integer streak = 0;
        LocalDate cursor = today;
        while (activeDays.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }
}
