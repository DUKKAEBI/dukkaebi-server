package com.ducami.dukkaebi.domain.user.presentation.dto.response;

import com.ducami.dukkaebi.domain.user.domain.User;
import lombok.Builder;

@Builder
public record UserInfoRes(
        Long id,
        String nickname,
        int score
) {
    public static UserInfoRes from(User user) {
        return UserInfoRes.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .score(user.getScore())
                .build();
    }
}
