package com.ducami.dukkaebi.domain.contest.presentation.controller;

import com.ducami.dukkaebi.domain.contest.presentation.dto.response.ContestDetailRes;
import com.ducami.dukkaebi.domain.contest.presentation.dto.response.ContestListRes;
import com.ducami.dukkaebi.domain.contest.usecase.ContestUseCase;
import com.ducami.dukkaebi.global.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "대회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/contest")
public class ContestController {
    private final ContestUseCase contestUseCase;

    @GetMapping("/list")
    @Operation(summary = "대회 목록")
    public PageResponse<ContestListRes> getContestList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return contestUseCase.getContestListPaged(page, size);
    }

    @GetMapping("/{code}")
    @Operation(summary = "대회 상세 조회")
    public ContestDetailRes getContestDetail(@PathVariable String code) {
        return contestUseCase.getContestDetail(code);
    }

    @GetMapping("/search")
    @Operation(summary = "이름으로 검색")
    public PageResponse<ContestListRes> getContestWithName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return contestUseCase.getContestWithNamePaged(name, page, size);
    }
}