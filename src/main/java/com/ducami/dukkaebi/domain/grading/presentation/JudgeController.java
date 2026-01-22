package com.ducami.dukkaebi.domain.grading.presentation;

import com.ducami.dukkaebi.domain.grading.presentation.dto.request.CodeSaveReq;
import com.ducami.dukkaebi.domain.grading.presentation.dto.request.CodeSubmitReq;
import com.ducami.dukkaebi.domain.grading.presentation.dto.request.CodeTestReq;
import com.ducami.dukkaebi.domain.grading.presentation.dto.response.CodeSaveRes;
import com.ducami.dukkaebi.domain.grading.presentation.dto.response.JudgeResultRes;
import com.ducami.dukkaebi.domain.grading.presentation.dto.response.SavedCodeRes;
import com.ducami.dukkaebi.domain.grading.usecase.JudgeUseCase;
import com.ducami.dukkaebi.domain.grading.usecase.SavedCodeUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "코드 채점 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/solve")
public class JudgeController {
    private final JudgeUseCase judgeUseCase;
    private final SavedCodeUseCase savedCodeUseCase;

    @PostMapping("/grading")
    @Operation(summary = "문제 제출하기", description = "language : java or python or cpp")
    public JudgeResultRes submitCode(@RequestBody CodeSubmitReq request) {
        return judgeUseCase.submitCode(request);
    }

    @PostMapping("/test")
    @Operation(summary = "코드 테스트하기 (제출 없이)", description = "language : java or python or cpp. 점수나 기록이 남지 않고 테스트만 수행합니다.")
    public JudgeResultRes testCode(@RequestBody CodeTestReq request) {
        return judgeUseCase.testCode(request);
    }

    @PostMapping("/save")
    @Operation(summary = "코드 저장하기", description = "사용자가 작성 중인 코드를 저장합니다. 이미 저장된 코드가 있으면 업데이트됩니다.")
    public CodeSaveRes saveCode(@RequestBody CodeSaveReq request) {
        return savedCodeUseCase.saveCode(request);
    }

    @GetMapping("/saved/{problemId}")
    @Operation(summary = "저장된 코드 불러오기", description = "해당 문제에 대해 사용자가 저장한 코드를 불러옵니다. 저장된 코드가 없으면 null을 반환합니다.")
    public SavedCodeRes getSavedCode(@PathVariable Long problemId) {
        return savedCodeUseCase.getSavedCode(problemId);
    }
}
