package com.ducami.dukkaebi.global.common.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
public class Response {
    private int status;
    private String message;

    public static Response of(HttpStatus status, String message) {
        return Response.builder()
                .status(status.value())
                .message(message)
                .build();
    }

    public static Response ok(String message) {
        return Response.builder()
                .status(HttpStatus.OK.value())
                .message(message)
                .build();
    }

    public static Response created(String message) {
        return Response.builder()
                .status(HttpStatus.CREATED.value())
                .message(message)
                .build();
    }

    public static Response noContent(String message) {
        return Response.builder()
                .status(HttpStatus.NO_CONTENT.value())
                .message(message)
                .build();
    }
}