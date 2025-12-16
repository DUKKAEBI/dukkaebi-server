package com.ducami.dukkaebi.domain.contest.presentation;

import com.ducami.dukkaebi.domain.contest.presentation.dto.request.ContestReq;
import com.ducami.dukkaebi.domain.contest.presentation.dto.response.ContestDetailRes;
import com.ducami.dukkaebi.domain.contest.presentation.dto.response.ContestListRes;
import com.ducami.dukkaebi.domain.contest.usecase.ContestUseCase;
import com.ducami.dukkaebi.global.common.Response;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/contest")
public class ContestController {
    private final ContestUseCase contestUseCase;

    @GetMapping("/list")
    @Operation(summary = "대회 목록")
    public List<ContestListRes> getContestList() {
        return contestUseCase.getContestList();
    }

    @GetMapping("/{code}")
    @Operation(summary = "대회 상세 조회")
    public ContestDetailRes getContestDetail(@PathVariable String code) {
        return contestUseCase.getContestDetail(code);
    }
}