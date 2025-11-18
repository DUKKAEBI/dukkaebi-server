package com.ducami.dukkaebi.domain.user.error;

import com.ducami.dukkaebi.global.exception.error.CustomErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements CustomErrorCode {
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "유저를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}
