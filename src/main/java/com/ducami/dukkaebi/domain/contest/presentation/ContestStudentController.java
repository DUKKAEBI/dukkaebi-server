package com.ducami.dukkaebi.domain.contest.presentation;

import com.ducami.dukkaebi.domain.contest.usecase.ContestUseCase;
import com.ducami.dukkaebi.global.common.Response;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/student/contest")
public class ContestStudentController {
    private final ContestUseCase contestUseCase;

    @PostMapping("/{code}/join")
    @Operation(summary = "대회 참가", description = "대회 코드로 참가")
    public Response join(@PathVariable String code) {
        return contestUseCase.joinContest(code);
    }
}
