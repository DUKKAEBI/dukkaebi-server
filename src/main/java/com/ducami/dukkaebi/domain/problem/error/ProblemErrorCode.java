package com.ducami.dukkaebi.domain.problem.error;

import com.ducami.dukkaebi.global.exception.error.CustomErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProblemErrorCode implements CustomErrorCode {
    PROBLEM_FETCH_FAILED(HttpStatus.BAD_REQUEST, "문제 목록 조회를 실패했습니다."),
    PROBLEM_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 문제를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}
