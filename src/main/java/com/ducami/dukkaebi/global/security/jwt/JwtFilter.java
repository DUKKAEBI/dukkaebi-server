package com.ducami.dukkaebi.global.security.jwt;

import com.ducami.dukkaebi.global.exception.CustomException;
import com.ducami.dukkaebi.global.exception.ErrorResponse;
import com.ducami.dukkaebi.global.security.jwt.error.JwtErrorCode;
import com.ducami.dukkaebi.global.security.jwt.util.JwtExtractor;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtExtractor jwtExtractor;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();

        // 인증 필요 없는 경로는 필터 지나가게
        if (uri.startsWith("/auth/")
                || uri.startsWith("/swagger-ui")           // 슬래시 없이: 전체 경로 포함
                || uri.startsWith("/v3/api-docs")           // JSON 문서 요청
                || uri.startsWith("/swagger-resources")     // Swagger 리소스
) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = jwtExtractor.getToken(request);
            if (token != null) {
                SecurityContextHolder.getContext().setAuthentication(jwtExtractor.getAuthentication(token));
            }
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            sendError(response, new CustomException(JwtErrorCode.EXPIRED_TOKEN));
        } catch (MalformedJwtException e) {
            sendError(response, new CustomException(JwtErrorCode.MALFORMED_TOKEN));
        } catch (CustomException e) {
            sendError(response, e);
        }
    }

    private void sendError(HttpServletResponse response, CustomException exception) throws IOException {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(exception.getError().getStatus().value())
                .message(exception.getError().getMessage())
                .build();

        response.setStatus(exception.getError().getStatus().value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (var outputStream = response.getOutputStream()) {
            outputStream.write(objectMapper.writeValueAsBytes(errorResponse));
            outputStream.flush();
        }
    }
}
