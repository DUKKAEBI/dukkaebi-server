package com.ducami.dukkaebi.domain.grading.error;

import com.ducami.dukkaebi.global.exception.error.CustomErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum JudgeError implements CustomErrorCode {
    JUDGE_FAILED(HttpStatus.BAD_REQUEST, "제출을 실패했습니다.");

    private final HttpStatus status;
    private final String message;
}
