package com.ducami.dukkaebi.domain.user.presentation.controller;

import com.ducami.dukkaebi.domain.user.usecase.UserUseCase;
import com.ducami.dukkaebi.global.common.dto.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "사용자 관리자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/user")
public class UserAdminController {
    private final UserUseCase userUseCase;

    @DeleteMapping("/delete/{userId}")
    @Operation(summary = "사용자 삭제 (관리자)")
    public Response deleteUserByAdmin(@PathVariable("userId") Long userId) {
       return userUseCase.deleteUserByAdmin(userId);
    }
}