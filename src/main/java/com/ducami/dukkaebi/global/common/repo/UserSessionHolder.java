package com.ducami.dukkaebi.global.common.repo;

import com.ducami.dukkaebi.domain.user.domain.User;
import com.ducami.dukkaebi.domain.user.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class UserSessionHolder {
    private final UserService userService;

    public User get() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.findByUserId(Long.valueOf(userId));
    }
}
