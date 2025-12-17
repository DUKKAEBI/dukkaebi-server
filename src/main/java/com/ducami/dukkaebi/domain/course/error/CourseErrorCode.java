package com.ducami.dukkaebi.domain.course.error;

import com.ducami.dukkaebi.global.exception.error.CustomErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CourseErrorCode implements CustomErrorCode {
    TITLE_ALREADY(HttpStatus.CONFLICT, "이미 존재하는 대회명입니다."),
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 코스를 찾을 수 없습니다."),
    PROBLEM_ALREADY_IN_COURSE(HttpStatus.CONFLICT, "이미 코스에 추가된 문제입니다."),
    PROBLEM_NOT_IN_COURSE(HttpStatus.BAD_REQUEST, "코스에 존재하지 않는 문제입니다.");

    private final HttpStatus status;
    private final String message;
}
