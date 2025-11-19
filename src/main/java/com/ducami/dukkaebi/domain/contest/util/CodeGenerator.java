package com.ducami.dukkaebi.domain.contest.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

//6자리 랜덤 코드 생성 코드
@Component
public class CodeGenerator {
    public String generateCode() {
        return UUID.randomUUID().toString()
                .replaceAll("-", "")
                .substring(0, 6);
    }
}