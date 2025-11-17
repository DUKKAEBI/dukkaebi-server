package com.ducami.dukkaebi.domain.auth.presentation;

import com.ducami.dukkaebi.domain.auth.presentation.dto.request.RefreshReq;
import com.ducami.dukkaebi.domain.auth.presentation.dto.request.SignInReq;
import com.ducami.dukkaebi.domain.auth.presentation.dto.request.SignUpReq;
import com.ducami.dukkaebi.domain.auth.presentation.dto.response.RefreshRes;
import com.ducami.dukkaebi.domain.auth.presentation.dto.response.SignInRes;
import com.ducami.dukkaebi.domain.auth.usecase.AuthUseCase;
import com.ducami.dukkaebi.global.common.Response;
import com.ducami.dukkaebi.global.common.ResponseData;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthUseCase authUseCase;

    @PostMapping("/sign-up")
    @Operation(summary = "회원가입")
    public Response signUp(@RequestBody SignUpReq req) {
        return authUseCase.signUp(req);
    }

    @PostMapping("/sign-in")
    @Operation(summary = "로그인", description = "리프레쉬 토큰: 1주, 액세스 토큰: 2시간")
    public SignInRes signIn(@Validated @RequestBody SignInReq req) {
        return authUseCase.signIn(req);
    }

    @PostMapping("/refresh")
    @Operation(summary = "액세스 토큰 재발급")
    public RefreshRes refresh(@Validated @RequestBody RefreshReq req) {
        return authUseCase.refresh(req);
    }
}
