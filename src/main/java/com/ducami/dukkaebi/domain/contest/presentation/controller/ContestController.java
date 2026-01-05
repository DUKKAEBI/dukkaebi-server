package com.ducami.dukkaebi.domain.contest.presentation.controller;

import com.ducami.dukkaebi.domain.contest.presentation.dto.response.ContestDetailRes;
import com.ducami.dukkaebi.domain.contest.presentation.dto.response.ContestListRes;
import com.ducami.dukkaebi.domain.contest.usecase.ContestUseCase;
import com.ducami.dukkaebi.domain.problem.presentation.dto.response.ProblemRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "대회 API")
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

    @GetMapping("/search")
    @Operation(summary = "이름으로 검색")
    public List<ContestListRes> getContestWithName(@RequestParam String name) {
        return contestUseCase.getContestWithName(name);
    }
}