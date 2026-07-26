package com.ducami.dukkaebi.domain.grading.presentation;

import com.ducami.dukkaebi.domain.grading.presentation.dto.response.SavedCodeRes;
import com.ducami.dukkaebi.domain.grading.usecase.JudgeUseCase;
import com.ducami.dukkaebi.domain.grading.usecase.SavedCodeUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class JudgeSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JudgeUseCase judgeUseCase;

    @MockitoBean
    private SavedCodeUseCase savedCodeUseCase;

    @Test
    void rejectsUnauthenticatedSubmit() throws Exception {
        mockMvc.perform(post("/solve/grading")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsUnauthenticatedCodeTest() throws Exception {
        mockMvc.perform(post("/solve/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsUnauthenticatedCodeSave() throws Exception {
        mockMvc.perform(post("/solve/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsUnauthenticatedSavedCodeLookup() throws Exception {
        mockMvc.perform(get("/solve/saved/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void allowsAuthenticatedSavedCodeLookup() throws Exception {
        when(savedCodeUseCase.getSavedCode(anyLong())).thenReturn(new SavedCodeRes(1L, "print(1)", "python", null));

        mockMvc.perform(get("/solve/saved/1").with(user("tester").roles("STUDENT")))
                .andExpect(status().isOk());
    }
}
