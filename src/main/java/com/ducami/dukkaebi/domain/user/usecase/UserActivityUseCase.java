package com.ducami.dukkaebi.domain.user.usecase;

import com.ducami.dukkaebi.domain.user.presentation.dto.response.StreakRes;
import com.ducami.dukkaebi.domain.user.service.UserActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActivityUseCase {
    private final UserActivityService userActivityService;

    public Map<String, Integer> getContributions(LocalDate start, LocalDate end) {
        var data = userActivityService.getContributions(start, end);
        Map<String, Integer> resp = new LinkedHashMap<>();
        data.forEach((k, v) -> resp.put(k.toString(), v)); // LocalDate -> ISO-8601 문자열 키
        return resp;
    }

    public StreakRes getStreak() {
        int streak = userActivityService.getCurrentStreak();
        return new StreakRes(streak);
    }
}

