package com.ducami.dukkaebi.global.exception.error;

import org.springframework.http.HttpStatus;

public interface CustomErrorCode {
    HttpStatus getStatus();
    String getMessage();
}
