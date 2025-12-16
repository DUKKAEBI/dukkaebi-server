package com.ducami.dukkaebi.domain.chatbot.presentation;

import com.ducami.dukkaebi.domain.chatbot.presentation.dto.request.ChatReq;
import com.ducami.dukkaebi.domain.chatbot.presentation.dto.response.ChatRes;
import com.ducami.dukkaebi.domain.chatbot.usecase.ChatbotUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "챗봇 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/chatbot")
public class ChatbotController {
    private final ChatbotUseCase chatbotUseCase;

    @PostMapping("/chat")
    @Operation(summary = "챗봇 대화")
    public ChatRes chat(@RequestBody ChatReq req) {
        return chatbotUseCase.chat(req);
    }
}
