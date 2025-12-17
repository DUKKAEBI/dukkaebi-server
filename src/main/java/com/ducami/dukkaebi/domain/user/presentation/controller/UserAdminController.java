package com.ducami.dukkaebi.domain.user.presentation.controller;

import com.ducami.dukkaebi.domain.user.usecase.UserUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "사용자 관리자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/user")
public class UserAdminController {
    private final UserUseCase userUseCase;

    @PatchMapping("/update/{userId}")
}
