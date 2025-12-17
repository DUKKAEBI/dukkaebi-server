package com.ducami.dukkaebi.domain.user.usecase;

import com.ducami.dukkaebi.domain.user.domain.User;
import com.ducami.dukkaebi.domain.user.domain.enums.UserType;
import com.ducami.dukkaebi.domain.user.domain.repo.UserJpaRepo;
import com.ducami.dukkaebi.domain.user.error.UserErrorCode;
import com.ducami.dukkaebi.domain.user.presentation.dto.response.UserInfoRes;
import com.ducami.dukkaebi.domain.user.presentation.dto.response.UserListRes;
import com.ducami.dukkaebi.domain.user.service.UserDeleteService;
import com.ducami.dukkaebi.global.common.Response;
import com.ducami.dukkaebi.global.exception.CustomException;
import com.ducami.dukkaebi.global.security.auth.UserSessionHolder;
import com.ducami.dukkaebi.global.security.jwt.service.JwtTokenService;
import com.ducami.dukkaebi.global.security.jwt.util.JwtExtractor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserUseCase {
    private final JwtExtractor jwtExtractor;
    private final JwtTokenService jwtTokenService;
    private final UserSessionHolder userSessionHolder;
    private final UserJpaRepo userJpaRepo;
    private final UserDeleteService userDeleteService;

    public UserInfoRes getUserInfo() {
        User user = userSessionHolder.getUser();
        return UserInfoRes.from(user);
    }

    public List<UserListRes> getAllUsers() {
        return userJpaRepo.findAll().stream()
                .filter(user -> user.getRole() == UserType.STUDENT)
                .map(UserListRes::from)
                .toList();
    }

    public UserInfoRes getUserInfoById(Long userId) {
        User user = userJpaRepo.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        return UserInfoRes.from(user);
    }

    public Response logout(HttpServletRequest req) {
        try {
            String token = jwtExtractor.getToken(req);

            if (token != null) {
                jwtTokenService.addToBlacklist(token);
            }

            SecurityContextHolder.clearContext();

            return Response.ok("로그아웃에 성공하였습니다.");
        } catch (Exception e) { // 토큰이 유효하지 않아도 로그아웃은 성공으로 처리
            SecurityContextHolder.clearContext();
            return Response.ok("로그아웃에 성공하였습니다.");
        }
    }

    public Response deleteUser() {
        Long userId = userSessionHolder.getUserId();
        return userDeleteService.deleteUserWithRelatedData(userId);
    }

    // 관리자
    public Response deleteUserByAdmin(Long userId) {
        User user = userJpaRepo.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        return userDeleteService.deleteUserWithRelatedData(userId);
    }
}
