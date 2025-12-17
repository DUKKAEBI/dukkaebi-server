package com.ducami.dukkaebi.domain.user.domain.repo;

import com.ducami.dukkaebi.domain.user.domain.UserDailyActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserDailyActivityJpaRepo extends JpaRepository<UserDailyActivity, Long> {
    Optional<UserDailyActivity> findByUser_IdAndActivityDate(Long userId, LocalDate activityDate);
    List<UserDailyActivity> findAllByUser_IdAndActivityDateBetweenOrderByActivityDate(Long userId, LocalDate start, LocalDate end);
    int deleteByUser_Id(Long userId);
}

