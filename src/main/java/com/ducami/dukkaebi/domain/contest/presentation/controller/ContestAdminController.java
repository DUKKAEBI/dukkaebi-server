package com.ducami.dukkaebi.domain.contest.presentation.controller;

import com.ducami.dukkaebi.domain.contest.presentation.dto.request.ContestReq;
import com.ducami.dukkaebi.domain.contest.presentation.dto.request.ContestScoreUpdateReq;
import com.ducami.dukkaebi.domain.contest.presentation.dto.response.ContestParticipantListRes;
import com.ducami.dukkaebi.domain.contest.usecase.ContestUseCase;
import com.ducami.dukkaebi.domain.problem.presentation.dto.request.ProblemCreateReq;
import com.ducami.dukkaebi.global.common.Response;
import com.ducami.dukkaebi.global.common.ResponseData;
import com.ducami.dukkaebi.global.common.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "대회 관리자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/contest")
public class ContestAdminController {
    private final ContestUseCase contestUseCase;
    private final S3Service s3Service;

    @PostMapping("/upload-image")
    @Operation(summary = "대회 표지 이미지 업로드")
    public ResponseData<String> uploadCoverImage(@RequestPart("file") MultipartFile file) {
        String imageUrl = s3Service.uploadFile(file, "contest");
        return ResponseData.created("이미지가 성공적으로 업로드되었습니다.", imageUrl);
    }

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

    @PostMapping("/{code}/problem/create")
    @Operation(summary = "대회 전용 문제 생성")
    public Response createContestProblem(@PathVariable("code") String code, @RequestBody ProblemCreateReq req) {
        return contestUseCase.createContestProblem(code, req);
    }

    @DeleteMapping("/{code}/problem/{problemId}")
    @Operation(summary = "대회 문제 삭제")
    public Response deleteContestProblem(@PathVariable("code") String code, @PathVariable("problemId") Long problemId) {
        return contestUseCase.deleteContestProblem(code, problemId);
    }

    @PatchMapping("/{code}/end")
    @Operation(summary = "대회 종료")
    public Response endContest(@PathVariable("code") String code) {
        return contestUseCase.endContest(code);
    }

    @GetMapping("/{code}/participants")
    @Operation(summary = "대회 참여자 목록 조회 (등수 순)")
    public List<ContestParticipantListRes> getContestParticipants(@PathVariable("code") String code) {
        return contestUseCase.getContestParticipants(code);
    }

    @PatchMapping("/{code}/participant/{userId}/score")
    @Operation(summary = "참여자 점수 수정")
    public Response updateParticipantScore(
            @PathVariable("code") String code,
            @PathVariable("userId") Long userId,
            @RequestBody ContestScoreUpdateReq req
    ) {
        return contestUseCase.updateParticipantScore(code, userId, req);
    }
}
