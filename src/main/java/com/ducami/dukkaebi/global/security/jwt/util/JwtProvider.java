package com.ducami.dukkaebi.global.security.jwt.util;

import com.ducami.dukkaebi.domain.auth.error.AuthErrorCode;
import com.ducami.dukkaebi.domain.auth.presentation.dto.response.RefreshRes;
import com.ducami.dukkaebi.domain.auth.presentation.dto.response.SignInRes;
import com.ducami.dukkaebi.domain.user.domain.User;
import com.ducami.dukkaebi.domain.user.domain.repo.UserJpaRepo;
import com.ducami.dukkaebi.global.exception.CustomException;
import com.ducami.dukkaebi.global.security.jwt.JwtProperties;
import com.ducami.dukkaebi.global.security.jwt.enums.TokenType;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtProvider {
    private final JwtProperties jwtProperties;
    private final UserJpaRepo userJpaRepo;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecretKey());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public SignInRes createToken(Long userId) {
        return SignInRes.builder()
                .accessToken(createAccessToken(userId))
                .refreshToken(createRefreshToken(userId))
                .build();
    }

    public String createAccessToken(Long userId) {
        User user = userJpaRepo.findById(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        return Jwts.builder()
                .header().add("typ", TokenType.ACCESS.toString()).and()
                .subject(String.valueOf(userId))
                .claim("role", user.getRole().name())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getAccessExp()))
                .signWith(getSigningKey())
                .compact();
    }

    private String createRefreshToken(Long userId) {
        User user = userJpaRepo.findById(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        return Jwts.builder()
                .header().add("typ", TokenType.REFRESH.toString()).and()
                .subject(String.valueOf(userId))
                .claim("role", user.getRole().name())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getRefreshExp()))
                .signWith(getSigningKey())
                .compact();
    }


    public RefreshRes refreshToken(Long userId) {
        return RefreshRes.builder()
                .accessToken(createAccessToken(userId)).build();
    }
}
