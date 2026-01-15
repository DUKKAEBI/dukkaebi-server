package com.ducami.dukkaebi.domain.user.usecase;

import com.ducami.dukkaebi.domain.user.domain.User;
import com.ducami.dukkaebi.domain.user.domain.enums.SortType;
import com.ducami.dukkaebi.domain.user.domain.repo.UserJpaRepo;
import com.ducami.dukkaebi.domain.user.error.UserErrorCode;
import com.ducami.dukkaebi.domain.user.presentation.dto.response.UserInfoRes;
import com.ducami.dukkaebi.domain.user.presentation.dto.response.UserInfoWithActivityRes;
import com.ducami.dukkaebi.domain.user.presentation.dto.response.UserListRes;
import com.ducami.dukkaebi.domain.user.service.UserDeleteService;
import com.ducami.dukkaebi.domain.user.service.UserFilterService;
import com.ducami.dukkaebi.global.common.dto.response.PageResponse;
import com.ducami.dukkaebi.global.common.dto.response.Response;
import com.ducami.dukkaebi.global.exception.CustomException;
import com.ducami.dukkaebi.global.security.auth.UserSessionHolder;
import com.ducami.dukkaebi.global.security.jwt.service.JwtTokenService;
import com.ducami.dukkaebi.global.security.jwt.util.JwtExtractor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserUseCase {
    private final JwtExtractor jwtExtractor;
    private final JwtTokenService jwtTokenService;
    private final UserSessionHolder userSessionHolder;
    private final UserJpaRepo userJpaRepo;
    private final UserDeleteService userDeleteService;
    private final UserFilterService userFilterService;
    private final UserActivityUseCase userActivityUseCase;

    public UserInfoRes getUserInfo() {
        User user = userSessionHolder.getUser();
        return UserInfoRes.from(user);
    }

    public PageResponse<UserListRes> getAllUsersPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserListRes> userPage = userJpaRepo.findAll(pageable)
                .map(UserListRes::from);
        return PageResponse.of(userPage);
    }

    public UserInfoRes getUserInfoById(Long userId) {
        User user = userJpaRepo.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        return UserInfoRes.from(user);
    }

    // 특정 사용자 정보 조회 (활동 데이터 포함)
    public UserInfoWithActivityRes getUserInfoWithActivity(Long userId, LocalDate start, LocalDate end) {
        User user = userJpaRepo.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Integer streak = userActivityUseCase.getStreakByUserId(userId).streak();

        Map<String, Integer> contributions = userActivityUseCase.getContributionsByUserId(userId, start, end);

        return UserInfoWithActivityRes.of(user, streak, contributions);
    }

    public List<UserListRes> getFilteredUsers(String keyword, SortType sortType) {
        List<User> users = userJpaRepo.findAll();
        return userFilterService.filterAndSortUsers(users, keyword, sortType).stream()
                .map(UserListRes::from)
                .toList();
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
