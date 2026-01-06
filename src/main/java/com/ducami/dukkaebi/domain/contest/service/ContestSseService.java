package com.ducami.dukkaebi.domain.contest.service;

import com.ducami.dukkaebi.domain.contest.presentation.dto.response.ContestUpdateEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContestSseService {
    private final ObjectMapper objectMapper;

    // 대회 코드별로 구독자(SseEmitter) 목록 관리
    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    // SSE 타임아웃 (30분)
    private static final Long DEFAULT_TIMEOUT = 30 * 60 * 1000L;

    /**
     * 새로운 SSE 연결 생성
     */
    public SseEmitter subscribe(String contestCode) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        // 대회별 구독자 목록에 추가
        emitters.computeIfAbsent(contestCode, k -> new CopyOnWriteArrayList<>()).add(emitter);

        log.info("SSE 연결 생성 - contestCode: {}, 현재 구독자 수: {}",
                contestCode, emitters.get(contestCode).size());

        // 연결 완료 이벤트 전송
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("SSE 연결이 완료되었습니다."));
        } catch (IOException e) {
            log.error("SSE 연결 초기화 실패: {}", e.getMessage());
        }

        // 타임아웃 처리
        emitter.onTimeout(() -> {
            log.info("SSE 타임아웃 - contestCode: {}", contestCode);
            removeEmitter(contestCode, emitter);
        });

        // 완료 처리
        emitter.onCompletion(() -> {
            log.info("SSE 연결 완료 - contestCode: {}", contestCode);
            removeEmitter(contestCode, emitter);
        });

        // 에러 처리
        emitter.onError((e) -> {
            log.error("SSE 에러 발생 - contestCode: {}, error: {}", contestCode, e.getMessage());
            removeEmitter(contestCode, emitter);
        });

        return emitter;
    }

    /**
     * 특정 대회의 모든 구독자에게 업데이트 이벤트 전송
     */
    public void sendUpdateEvent(String contestCode, ContestUpdateEvent event) {
        List<SseEmitter> contestEmitters = emitters.get(contestCode);

        if (contestEmitters == null || contestEmitters.isEmpty()) {
            log.info("SSE 이벤트 전송 대상 없음 - contestCode: {}", contestCode);
            return;
        }

        log.info("SSE 이벤트 전송 - contestCode: {}, 구독자 수: {}",
                contestCode, contestEmitters.size());

        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();

        contestEmitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("contest-update")
                        .data(objectMapper.writeValueAsString(event)));
            } catch (IOException e) {
                log.error("SSE 이벤트 전송 실패: {}", e.getMessage());
                deadEmitters.add(emitter);
            }
        });

        // 실패한 emitter 제거
        deadEmitters.forEach(emitter -> removeEmitter(contestCode, emitter));
    }

    /**
     * Emitter 제거
     */
    private void removeEmitter(String contestCode, SseEmitter emitter) {
        List<SseEmitter> contestEmitters = emitters.get(contestCode);
        if (contestEmitters != null) {
            contestEmitters.remove(emitter);
            log.info("SSE Emitter 제거 - contestCode: {}, 남은 구독자 수: {}",
                    contestCode, contestEmitters.size());

            // 구독자가 없으면 맵에서 제거
            if (contestEmitters.isEmpty()) {
                emitters.remove(contestCode);
            }
        }
    }

    /**
     * 특정 대회의 구독자 수 조회
     */
    public int getSubscriberCount(String contestCode) {
        List<SseEmitter> contestEmitters = emitters.get(contestCode);
        return contestEmitters != null ? contestEmitters.size() : 0;
    }
}

