package com.ducami.dukkaebi.domain.chatbot.usecase;

import com.ducami.dukkaebi.domain.chatbot.error.ChatbotErrorCode;
import com.ducami.dukkaebi.domain.chatbot.presentation.dto.request.ChatReq;
import com.ducami.dukkaebi.domain.chatbot.presentation.dto.response.ChatRes;
import com.ducami.dukkaebi.domain.chatbot.service.GeminiService;
import com.ducami.dukkaebi.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotUseCase {
    private final GeminiService geminiService;

    public ChatRes chat(ChatReq req) {
        try {
            String response = geminiService.generateResponse(req.message());
            return ChatRes.builder()
                    .response(response)
                    .build();
        } catch (Exception e) {
            log.error("챗봇 응답 생성 실패: {}", e.getMessage(), e);
            throw new CustomException(ChatbotErrorCode.CHATBOT_ERROR);
        }
    }
}
