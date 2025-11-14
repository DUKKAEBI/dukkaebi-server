package com.ducami.dukkaebi.global.exception;

import com.ducami.dukkaebi.global.exception.error.CustomErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CustomException extends RuntimeException {
    private final CustomErrorCode error;
}
