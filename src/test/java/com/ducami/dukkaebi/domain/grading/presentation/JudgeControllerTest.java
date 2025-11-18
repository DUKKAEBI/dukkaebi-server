package com.ducami.dukkaebi.domain.grading.presentation;

import com.ducami.dukkaebi.domain.grading.domain.enums.JudgeStatus;
import com.ducami.dukkaebi.domain.grading.presentation.dto.request.CodeSubmitReq;
import com.ducami.dukkaebi.domain.grading.presentation.dto.response.JudgeResultRes;
import com.ducami.dukkaebi.domain.grading.usecase.JudgeUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(MockitoExtension.class)
class JudgeControllerTest {

    MockMvc mockMvc;

    @Mock
    JudgeUseCase judgeUseCase;

    @InjectMocks
    JudgeController judgeController;

    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(judgeController).build();
    }

    @Test
    void submitCode() throws Exception {

        CodeSubmitReq req = new CodeSubmitReq(
                1L,
                "public class Main {}",
                "java"
        );

        JudgeResultRes mockRes = new JudgeResultRes(
                JudgeStatus.ACCEPTED,
                1,
                1,
                10L,
                null,
                List.of()
        );

        Mockito.when(judgeUseCase.submitCode(any()))
                .thenReturn(mockRes);

        mockMvc.perform(
                        post("/solve/grading")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void submitHelloWorld_java_returnsAccepted() throws Exception {
        // Java 코드(Hello World)
        String javaHello = """
            public class Main {
                public static void main(String[] args) {
                    System.out.println("Hello World");
                }
            }
            """;

        CodeSubmitReq req = new CodeSubmitReq(1L, javaHello, "java");

        // Mocked response: ACCEPTED
        JudgeResultRes mocked = new JudgeResultRes(
                JudgeStatus.ACCEPTED,
                1,
                1,
                10L,
                null,
                List.of()
        );

        Mockito.when(judgeUseCase.submitCode(any())).thenReturn(mocked);

        mockMvc.perform(post("/solve/grading")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.passedTestCases").value(1));
    }


    @Test
    void submitHelloWorld_python_returnsAccepted() throws Exception {
        // Python 코드(Hello World)
        String pyHello = "print(\"Hello World\")\n";

        CodeSubmitReq req = new CodeSubmitReq(1L, pyHello, "python");

        JudgeResultRes mocked = new JudgeResultRes(
                JudgeStatus.ACCEPTED,
                1,
                1,
                5L,
                null,
                List.of()
        );

        Mockito.when(judgeUseCase.submitCode(any())).thenReturn(mocked);

        mockMvc.perform(post("/solve/grading")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.passedTestCases").value(1));
    }
}
