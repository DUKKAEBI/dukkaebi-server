package com.ducami.dukkaebi.domain.auth.usecase;

import com.ducami.dukkaebi.domain.auth.error.AuthErrorCode;
import com.ducami.dukkaebi.domain.auth.presentation.dto.request.RefreshReq;
import com.ducami.dukkaebi.domain.auth.presentation.dto.request.SignInReq;
import com.ducami.dukkaebi.domain.auth.presentation.dto.request.SignUpReq;
import com.ducami.dukkaebi.domain.auth.presentation.dto.response.RefreshRes;
import com.ducami.dukkaebi.domain.auth.presentation.dto.response.SignInRes;
import com.ducami.dukkaebi.domain.user.domain.User;
import com.ducami.dukkaebi.domain.user.domain.repo.UserJpaRepo;
import com.ducami.dukkaebi.global.common.dto.response.Response;
import com.ducami.dukkaebi.global.exception.CustomException;
import com.ducami.dukkaebi.global.security.jwt.enums.TokenType;
import com.ducami.dukkaebi.global.security.jwt.error.JwtErrorCode;
import com.ducami.dukkaebi.global.security.jwt.util.JwtExtractor;
import com.ducami.dukkaebi.global.security.jwt.util.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthUseCase {
    private final UserJpaRepo userJpaRepo;
    private final JwtExtractor jwtExtractor;
    private final JwtProvider jwtProvider;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public Response signUp(SignUpReq req) {
        if(userJpaRepo.existsByLoginId(req.loginId())) {
            throw new CustomException(AuthErrorCode.ID_ALREADY);
        }
        if (userJpaRepo.existsByNickname(req.nickname())) {
            throw new CustomException(AuthErrorCode.NICKNAME_ALREADY);
        }

        userJpaRepo.save(SignUpReq.fromSignUpReq(req, passwordEncoder.encode(req.password())));

        return Response.created("회원가입에 성공하였습니다.");
    }

    public SignInRes signIn(SignInReq req) {
        User user = userJpaRepo.findByLoginId(req.loginId())
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new CustomException(AuthErrorCode.WRONG_PASSWORD);
        }

        return jwtProvider.createToken(user.getId());
    }

    public RefreshRes refresh(RefreshReq req) {
        if (jwtExtractor.isWrongType(req.refreshToken(), TokenType.REFRESH)) {
            throw new  CustomException(JwtErrorCode.TOKEN_TYPE_ERROR);
        }

        Long userId = jwtExtractor.getUserId(req.refreshToken());

        return jwtProvider.refreshToken(userId);
    }
}