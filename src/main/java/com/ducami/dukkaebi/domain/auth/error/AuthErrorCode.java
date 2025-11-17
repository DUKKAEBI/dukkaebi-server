package com.ducami.dukkaebi.domain.auth.error;

import com.ducami.dukkaebi.global.exception.error.CustomErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements CustomErrorCode {
    NICKNAME_ALREADY(HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다."),
    ID_ALREADY(HttpStatus.CONFLICT, "이미 존재하는 아이디입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다."),
    WRONG_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 맞지 않습니다.");

    private final HttpStatus status;
    private final String message;
}
