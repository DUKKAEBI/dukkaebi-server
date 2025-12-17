package com.ducami.dukkaebi.domain.user.presentation.dto.response;

import com.ducami.dukkaebi.domain.user.domain.User;
import com.ducami.dukkaebi.domain.user.domain.enums.GrowthType;

public record UserListRes(
    Long id,
    String loginId,
    String nickname,
    GrowthType growth
) {
    public static UserListRes from(User user) {
        return new UserListRes(
                user.getId(),
                user.getLoginId(),
                user.getNickname(),
                user.getGrowth()
        );
    }
}
