package com.ducami.dukkaebi.domain.chatbot.service;

import com.ducami.dukkaebi.domain.chatbot.error.ChatbotErrorCode;
import com.ducami.dukkaebi.global.exception.CustomException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {
    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.api-url}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String RATE_LIMITER_NAME = "geminiLimiter";
    private static final String RETRY_NAME = "geminiRetry";

    private static final String SYSTEM_INSTRUCTION = """
            당신은 '두비'라는 이름의 코딩 학습 도우미 챗봇입니다.
            
            필수 규칙:
            1. 항상 부드럽고 공손한 존댓말을 사용해야 합니다. 반말은 절대 사용하지 마세요.
            2. 이모티콘은 사용하지 마세요.
            3. 이름을 묻는 질문에는 "저의 이름은 두비에요!"라고만 답변하세요.
            4. 주로 코딩 관련 질문에 답변하세요. (프로그래밍 언어, 알고리즘, 개발 도구, 웹/앱 개발 등)
            5. 코딩과 관련 없는 질문에는 "죄송하지만, 저는 코딩 관련 질문에만 답변할 수 있어요. 프로그래밍에 대해 궁금한 점이 있으시면 편하게 물어보세요!"라고 정중히 안내하세요.
            6. 욕설이나 부적절한 표현이 포함된 질문에는 "적절하지 않은 표현은 사용하지 말아주세요. 코딩에 대해 궁금하신 점을 정중하게 물어봐 주시면 감사하겠습니다."라고 답변하세요.
            7. 코드 예제를 제공할 때는 설명과 함께 친절하게 알려주세요.
            8. 학생의 학습을 돕는 것이 목표이므로, 단순히 답을 알려주기보다는 이해를 돕는 방식으로 설명해주세요.
            9. 질문이 불명확하면 구체적으로 어떤 부분이 궁금한지 정중하게 되물어보세요.
            10. 답변은 간결하면서도 충분한 정보를 담아 제공하세요.
            """;

    @RateLimiter(name = RATE_LIMITER_NAME)
    @Retry(name = RETRY_NAME)
    public String generateResponse(String userMessage) {
        try {
            String url = apiUrl + "?key=" + apiKey;

            Map<String, Object> requestBody = buildRequestBody(userMessage);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            return parseResponse(response.getBody());

        } catch (HttpClientErrorException.TooManyRequests e) {
            log.error("429 Too Many Requests 발생 — Retry 시도 중...");
            throw e; // Retry가 잡아서 재시도함
        } catch (HttpClientErrorException e) {
            // 상세 오류 본문 로깅
            log.error("Gemini API 호출 실패: {} - body: {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new CustomException(ChatbotErrorCode.API_CALL_FAILED);
        } catch (Exception e) {
            log.error("Gemini API 호출 실패: {}", e.getMessage(), e);
            throw new CustomException(ChatbotErrorCode.API_CALL_FAILED);
        }
    }

    private Map<String, Object> buildRequestBody(String userMessage) {
        Map<String, Object> body = new HashMap<>();

        // 1. 시스템 지시문 설정
        Map<String, Object> instructionContent = new HashMap<>();
        instructionContent.put("role", "user");
        instructionContent.put("parts", List.of(Map.of("text", SYSTEM_INSTRUCTION)));

        // 2. 사용자 메시지 설정
        Map<String, Object> userContent = new HashMap<>();
        userContent.put("role", "user");
        userContent.put("parts", List.of(Map.of("text", userMessage)));

        body.put("contents", List.of(instructionContent, userContent));

        // 3. 생성 설정
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("maxOutputTokens", 1000);
        body.put("generationConfig", generationConfig);

        return body;
    }

    private String parseResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");

            if (candidates.isEmpty()) {
                throw new CustomException(ChatbotErrorCode.EMPTY_RESPONSE);
            }

            String text = candidates.get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

            if (text == null || text.isBlank()) {
                throw new CustomException(ChatbotErrorCode.EMPTY_RESPONSE);
            }

            return text;
        } catch (Exception e) {
            log.error("응답 파싱 실패: {}", e.getMessage(), e);
            throw new CustomException(ChatbotErrorCode.RESPONSE_PARSE_ERROR);
        }
    }
}