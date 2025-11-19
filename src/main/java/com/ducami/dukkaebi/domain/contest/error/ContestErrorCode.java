package com.ducami.dukkaebi.domain.contest.error;

import com.ducami.dukkaebi.global.exception.error.CustomErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ContestErrorCode implements CustomErrorCode {
    TITLE_ALREADY(HttpStatus.CONFLICT, "이미 존재하는 대회명입니다."),
    CONTEST_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 대회를 찾을 수 없습니다.");


    private final HttpStatus status;
    private final String message;
}
