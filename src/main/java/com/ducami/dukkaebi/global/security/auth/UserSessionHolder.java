package com.ducami.dukkaebi.global.security.auth;

import com.ducami.dukkaebi.domain.auth.error.AuthErrorCode;
import com.ducami.dukkaebi.domain.user.domain.User;
import com.ducami.dukkaebi.domain.user.domain.repo.UserJpaRepo;
import com.ducami.dukkaebi.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserSessionHolder{
    private final UserJpaRepo userJpaRepo;

    public User getUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof AuthDetails authDetails) {
            return authDetails.getUser();
        } else {
            throw new CustomException(AuthErrorCode.USER_NOT_FOUND);
        }
    }

    // 편의 메서드: 자주 쓰는 사용자 ID 바로 반환
    public Long getUserId() {
        return getUser().getId();
    }
}
