package com.ducami.dukkaebi.domain.auth.presentation.dto.request;

import com.ducami.dukkaebi.domain.user.domain.User;
import com.ducami.dukkaebi.domain.user.domain.enums.GrowthType;
import com.ducami.dukkaebi.domain.user.domain.enums.UserType;

public record SignUpReq(
        String loginId,
        String password,
        String  nickname) {
    public static User fromSignUpReq(SignUpReq req, String password) {
        return User.builder()
                .loginId(req.loginId)
                .password(password)
                .nickname(req.nickname)
                .role(UserType.STUDENT)
                .score(0)
                .growth(GrowthType.WISP)
                .build();
    }
}
