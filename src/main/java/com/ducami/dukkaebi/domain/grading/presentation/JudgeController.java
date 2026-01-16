package com.ducami.dukkaebi.domain.grading.presentation;

import com.ducami.dukkaebi.domain.grading.presentation.dto.request.CodeSubmitReq;
import com.ducami.dukkaebi.domain.grading.presentation.dto.request.CodeTestReq;
import com.ducami.dukkaebi.domain.grading.presentation.dto.response.JudgeResultRes;
import com.ducami.dukkaebi.domain.grading.usecase.JudgeUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "코드 채점 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/solve")
public class JudgeController {
    private final JudgeUseCase judgeUseCase;

    @PostMapping("/grading")
    @Operation(summary = "문제 제출하기", description = "language : java or python")
    public JudgeResultRes submitCode(@RequestBody CodeSubmitReq request) {
        return judgeUseCase.submitCode(request);
    }

    @PostMapping("/test")
    @Operation(summary = "코드 테스트하기 (제출 없이)", description = "language : java or python. 점수나 기록이 남지 않고 테스트만 수행합니다.")
    public JudgeResultRes testCode(@RequestBody CodeTestReq request) {
        return judgeUseCase.testCode(request);
    }
}
