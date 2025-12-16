package com.ducami.dukkaebi.domain.contest.presentation;

import com.ducami.dukkaebi.domain.contest.presentation.dto.request.ContestReq;
import com.ducami.dukkaebi.domain.contest.usecase.ContestUseCase;
import com.ducami.dukkaebi.global.common.Response;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/contest")
public class ContestAdminController {
    private final ContestUseCase contestUseCase;

    @PostMapping("/create")
    @Operation(summary = "대회 생성")
    public Response createContest(@RequestBody ContestReq req) {
        return contestUseCase.createContest(req);
    }

    @PatchMapping("/update/{code}")
    @Operation(summary = "대회 수정")
    public Response updateContest(@PathVariable("code") String code, @RequestBody ContestReq req) {
        return contestUseCase.updateContest(code, req);
    }

    @DeleteMapping("/delete/{code}")
    @Operation(summary = "대회 삭제")
    public Response deleteContest(@PathVariable("code") String code) {
        return contestUseCase.deleteContest(code);
    }
}
