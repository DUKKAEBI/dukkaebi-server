package com.ducami.dukkaebi.domain.user.presentation.dto.response;

import com.ducami.dukkaebi.domain.user.domain.User;
import com.ducami.dukkaebi.domain.user.domain.enums.GrowthType;
import lombok.Builder;

import java.util.Map;

@Builder
public record UserInfoWithActivityRes(
        Long id,
        String nickname,
        Integer score,
        GrowthType growth,
        Integer streak,
        Map<String, Integer> contributions
) {
    public static UserInfoWithActivityRes of(User user, Integer streak, Map<String, Integer> contributions) {
        return UserInfoWithActivityRes.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .score(user.getScore())
                .growth(user.getGrowth())
                .streak(streak)
                .contributions(contributions)
                .build();
    }
}

