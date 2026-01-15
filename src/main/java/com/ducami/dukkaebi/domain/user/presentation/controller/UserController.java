package com.ducami.dukkaebi.domain.user.presentation.controller;

import com.ducami.dukkaebi.domain.user.domain.enums.SortType;
import com.ducami.dukkaebi.domain.user.presentation.dto.response.UserInfoRes;
import com.ducami.dukkaebi.domain.user.presentation.dto.response.UserInfoWithActivityRes;
import com.ducami.dukkaebi.domain.user.presentation.dto.response.UserListRes;
import com.ducami.dukkaebi.domain.user.usecase.UserUseCase;
import com.ducami.dukkaebi.global.common.dto.response.PageResponse;
import com.ducami.dukkaebi.global.common.dto.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "사용자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserUseCase userUseCase;

    @GetMapping
    @Operation(summary = "마이 페이지")
    public UserInfoRes getUserInfo() {
        return userUseCase.getUserInfo();
    }

    @GetMapping("/list")
    @Operation(summary = "전체 사용자 목록 조회(학생만)")
    public PageResponse<UserListRes> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        return userUseCase.getAllUsersPaged(page, size);
    }

    @GetMapping("/info/{userId}")
    @Operation(summary = "특정 사용자 정보 조회")
    public UserInfoRes getUserInfoById(@PathVariable("userId") Long userId) {
        return userUseCase.getUserInfoById(userId);
    }

    @GetMapping("/info/{userId}/activity")
    @Operation(summary = "특정 사용자 정보 조회 (활동 데이터 포함)")
    public UserInfoWithActivityRes getUserInfoWithActivity(
            @PathVariable("userId") Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return userUseCase.getUserInfoWithActivity(userId, start, end);
    }

    @GetMapping("/list/filter")
    @Operation(
            summary = "사용자 목록 검색 및 정렬",
            description = """
                    - keyword: 닉네임 또는 로그인 아이디로 검색
                    - sortBy: 정렬 기준 (NICKNAME, LOGIN_ID, GROWTH)
                    """
    )
    public List<UserListRes> getFilteredUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) SortType sortBy
    ) {
        return userUseCase.getFilteredUsers(keyword, sortBy);
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "토큰 재사용 불가, 다시 로그인")
    public Response logout(HttpServletRequest req) {
        return userUseCase.logout(req);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "회원 탈퇴")
    public Response deleteUser() {
        return userUseCase.deleteUser();
    }
}
