package com.ducami.dukkaebi.domain.contest.presentation;

import com.ducami.dukkaebi.domain.contest.presentation.dto.request.ContestReq;
import com.ducami.dukkaebi.domain.contest.presentation.dto.response.ContestListRes;
import com.ducami.dukkaebi.domain.contest.usecase.ContestUseCase;
import com.ducami.dukkaebi.global.common.Response;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/contest")
public class ContestController {
    private final ContestUseCase contestUseCase;

    @GetMapping("/list")
    @Operation(summary = "대회 목록 조회")
    public List<ContestListRes> getContestList() {
        return contestUseCase.getContestList();
    }

    @PostMapping("/create")
    @Operation(summary = "대회 생성")
    public Response createContest(@RequestBody ContestReq req) {
        return contestUseCase.createContest(req);
    }

    @PostMapping("/join")
    @Operation(summary = "대회 참가", description = "대회 코드로 참가")
    public Response join(@RequestParam String code) {
        return contestUseCase.joinContest(code);
    }
}
