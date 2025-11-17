package com.ducami.dukkaebi.domain.user.presentation;

import com.ducami.dukkaebi.domain.user.usecase.UserUseCase;
import com.ducami.dukkaebi.global.common.Response;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserUseCase userUseCase;

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "토큰 재사용 불가, 다시 로그인")
    public Response logout(HttpServletRequest req) {
        return userUseCase.logout(req);
    }
}
