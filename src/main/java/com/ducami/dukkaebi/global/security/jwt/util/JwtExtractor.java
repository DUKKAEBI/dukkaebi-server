package com.ducami.dukkaebi.global.security.jwt.util;


import com.ducami.dukkaebi.domain.auth.error.AuthErrorCode;
import com.ducami.dukkaebi.domain.user.domain.User;
import com.ducami.dukkaebi.domain.user.domain.repo.UserJpaRepo;
import com.ducami.dukkaebi.global.exception.CustomException;
import com.ducami.dukkaebi.global.security.auth.AuthDetails;
import com.ducami.dukkaebi.global.security.jwt.JwtProperties;
import com.ducami.dukkaebi.global.security.jwt.enums.TokenType;
import com.ducami.dukkaebi.global.security.jwt.error.JwtErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
@RequiredArgsConstructor
public class JwtExtractor {
    private final JwtProperties jwtProperties;
    private final UserJpaRepo userJpaRepo;
    private final StringRedisTemplate redisTemplate;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecretKey());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String getToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    public Authentication getAuthentication(String token) {
        if (isTokenBlacklisted(token)) {
            throw new CustomException(JwtErrorCode.INVALID_TOKEN);
        }

        Jws<Claims> claims = getClaims(token);
        Long userId = Long.valueOf(claims.getPayload().getSubject());

        User user = userJpaRepo.findById(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        AuthDetails details = new AuthDetails(user);

        return new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
    }

    public Long getUserId(String token) {
        return Long.valueOf(getClaims(token).getPayload().getSubject());
    }

    public boolean isWrongType(String token, TokenType tokenType) {
        Jws<Claims> claims = getClaims(token);
        Object header = claims.getHeader().get("typ");

        return !tokenType.toString().equals(String.valueOf(header));
    }

    public Jws<Claims> getClaims(String token) {
        try{
            return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
        } catch (ExpiredJwtException e) {
            throw new CustomException(JwtErrorCode.EXPIRED_TOKEN);
        } catch (UnsupportedJwtException e) {
            throw new CustomException(JwtErrorCode.UNSUPPORTED_TOKEN);
        } catch (IllegalArgumentException e) {
            throw new CustomException(JwtErrorCode.INVALID_TOKEN);
        } catch (MalformedJwtException e) {
            throw new CustomException(JwtErrorCode.MALFORMED_TOKEN);
        }
    }

    private boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token));
    }
}
