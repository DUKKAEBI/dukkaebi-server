package com.ducami.dukkaebi.domain.user.presentation.dto.response;

import com.ducami.dukkaebi.domain.user.domain.User;
import com.ducami.dukkaebi.domain.user.domain.enums.GrowthType;
import lombok.Builder;

@Builder
public record UserInfoRes(
        Long id,
        String nickname,
        Integer score,
        GrowthType growth
) {
    public static UserInfoRes from(User user) {
        return UserInfoRes.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .score(user.getScore())
                .growth(user.getGrowth())
                .build();
    }
}
