package com.ducami.dukkaebi.domain.notice.presentation.controller;

import com.ducami.dukkaebi.domain.notice.presentation.dto.request.NoticeReq;
import com.ducami.dukkaebi.domain.notice.usecase.NoticeUseCase;
import com.ducami.dukkaebi.global.common.Response;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "공지 관리자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/notice")
public class NoticeAdminController {
    private final NoticeUseCase noticeUseCase;

    @PostMapping()
    public ResponseEntity<Response> createNotice(@ModelAttribute NoticeReq req) {
        Response response = noticeUseCase.createNotice(req);
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping(value = "/{noticeId}")
    public ResponseEntity<Response> updateNotice(
            @PathVariable Long noticeId,
            @ModelAttribute NoticeReq req
    ) {
        Response response = noticeUseCase.updateNotice(noticeId, req);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{noticeId}")
    public ResponseEntity<Response> deleteNotice(@PathVariable Long noticeId) {
        Response response = noticeUseCase.deleteNotice(noticeId);
        return ResponseEntity.ok(response);
    }
}
