package com.ducami.dukkaebi.global.security.jwt.error;

import com.ducami.dukkaebi.global.exception.error.CustomErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum JwtErrorCode implements CustomErrorCode {
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "지원하지 않는 토큰입니다."),
    INCORRECT_TOKEN(HttpStatus.UNAUTHORIZED, "잘못된 토큰입니다."),
    MALFORMED_TOKEN(HttpStatus.UNAUTHORIZED, "잘못된 토큰입니다."),
    TOKEN_TYPE_ERROR(HttpStatus.BAD_REQUEST,"잘못된 토큰 타입입니다.");

    private final HttpStatus status;
    private final String message;
}
