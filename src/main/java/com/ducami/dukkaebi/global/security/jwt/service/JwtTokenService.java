package com.ducami.dukkaebi.global.security.jwt.service;

import com.ducami.dukkaebi.global.security.jwt.util.JwtExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class JwtTokenService {
    private final StringRedisTemplate redisTemplate;
    private final JwtExtractor jwtExtractor;

    public void addToBlacklist(String token) {
        try {
            long remainingTime = getRemainingExpiration(token);

            if (remainingTime > 0) {
                redisTemplate.opsForValue().set(
                        "blacklist:" + token,
                        "true",
                        remainingTime,
                        TimeUnit.MILLISECONDS
                );
            }
        } catch (Exception e) {
            System.out.println("블랙리스트 추가 실패: " + e.getMessage());
        }
    }

    private long getRemainingExpiration(String token) {
        try {
            var claims = jwtExtractor.getClaims(token);
            long expiration = claims.getPayload().getExpiration().getTime();
            long now = System.currentTimeMillis();

            return Math.max(0, expiration - now);
        } catch (Exception e) {
            return 0;
        }
    }
}