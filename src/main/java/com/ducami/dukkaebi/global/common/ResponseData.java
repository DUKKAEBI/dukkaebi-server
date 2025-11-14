package com.ducami.dukkaebi.global.common;

import com.ducami.dukkaebi.global.exception.error.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ResponseData<T> extends Response {
    private final T data;

    private ResponseData(HttpStatus status, String message, T data) {
        super(status.value(), message);
        this.data = data;
    }

    public static <T> ResponseData<T> of(HttpStatus status, String message, T data) {
        return new ResponseData<>(status, message, data);
    }

    public static <T> ResponseData<T> ok(String message, T data) {
        return new ResponseData<>(HttpStatus.OK, message, data);
    }

    public static <T> ResponseData<T> created(String message, T data) {
        return new ResponseData<>(HttpStatus.CREATED, message, data);
    }

    public static <T> ResponseData<T> error(ErrorCode errorCode, String message) {
        return new ResponseData<>(errorCode.getStatus(), message, null);
    }
}