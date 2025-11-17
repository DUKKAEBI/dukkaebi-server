package com.ducami.dukkaebi.domain.chatbot.error;

import com.ducami.dukkaebi.global.exception.error.CustomErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ChatbotErrorCode implements CustomErrorCode {
    CHATBOT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "챗봇 응답 생성에 실패했습니다."),
    API_CALL_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "AI 서비스 호출에 실패했습니다."),
    EMPTY_RESPONSE(HttpStatus.NO_CONTENT, "AI로부터 응답을 받지 못했습니다."),
    RESPONSE_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AI 응답 처리 중 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;
}
