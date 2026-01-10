package com.ducami.dukkaebi.domain.contest.presentation.controller;

import com.ducami.dukkaebi.domain.contest.service.ContestSseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletResponse;

@Slf4j
@Tag(name = "대회 SSE API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/contest")
public class ContestSseController {
    private final ContestSseService contestSseService;

    @GetMapping(value = "/{code}/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "대회 실시간 업데이트 구독")
    public SseEmitter subscribe(@PathVariable("code") String code, HttpServletResponse response) {
        log.info("대회 SSE 구독 요청 - contestCode: {}", code);

        // SSE를 위한 HTTP 응답 헤더 설정
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("X-Accel-Buffering", "no"); // Nginx 버퍼링 방지
        response.setHeader("Connection", "keep-alive");

        return contestSseService.subscribe(code);
    }
}

