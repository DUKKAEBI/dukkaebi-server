package com.ducami.dukkaebi.domain.user.presentation.controller;

import com.ducami.dukkaebi.domain.user.presentation.dto.response.StreakRes;
import com.ducami.dukkaebi.domain.user.usecase.UserActivityUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Tag(name = "사용자 활동 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/user/activity")
public class UserActivityController {
    private final UserActivityUseCase userActivityUseCase;

    @GetMapping("/contributions")
    @Operation(summary = "잔디 데이터", description = "날짜별 푼 문제 수를 반환합니다.")
    public Map<String, Integer> getContributions(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return userActivityUseCase.getContributions(start, end);
    }

    @GetMapping("/streak")
    @Operation(summary = "연속 학습일", description = "오늘 기준 연속으로 문제를 푼 일수를 반환합니다.")
    public StreakRes getStreak() {
        return userActivityUseCase.getStreak();
    }
}
