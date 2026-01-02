package com.ducami.dukkaebi.domain.notice.error;

import com.ducami.dukkaebi.global.exception.error.CustomErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NoticeErrorCode implements CustomErrorCode {
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND ,"공지사항을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}
