package com.ducami.dukkaebi.domain.notice.presentation.controller;

import com.ducami.dukkaebi.domain.notice.presentation.dto.request.NoticeReq;
import com.ducami.dukkaebi.domain.notice.usecase.NoticeUseCase;
import com.ducami.dukkaebi.global.common.Response;
import com.ducami.dukkaebi.global.common.ResponseData;
import com.ducami.dukkaebi.global.common.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "공지 관리자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/notice")
public class NoticeAdminController {
    private final NoticeUseCase noticeUseCase;
    private final S3Service s3Service;

    @PostMapping("/upload-file")
    @Operation(summary = "공지사항 첨부파일 업로드")
    public ResponseData<String> uploadFile(@RequestPart("file") MultipartFile file) {
        String fileUrl = s3Service.uploadFile(file, "notice");
        return ResponseData.created("파일이 성공적으로 업로드되었습니다.", fileUrl);
    }

    @PostMapping("/create")
    @Operation(summary = "공지사항 생성")
    public Response createNotice(@RequestBody NoticeReq req) {
        return noticeUseCase.createNotice(req);
    }

    @PutMapping(value = "/update/{noticeId}")
    @Operation(summary = "공지사항 수정")
    public Response updateNotice(
            @PathVariable Long noticeId,
            @RequestBody NoticeReq req
    ) {
        return noticeUseCase.updateNotice(noticeId, req);
    }

    @DeleteMapping("/delete/{noticeId}")
    @Operation(summary = "공지사항 삭제")
    public Response deleteNotice(@PathVariable Long noticeId) {
        return noticeUseCase.deleteNotice(noticeId);
    }
}
