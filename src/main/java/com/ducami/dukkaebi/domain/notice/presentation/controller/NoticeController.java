package com.ducami.dukkaebi.domain.notice.presentation.controller;

import com.ducami.dukkaebi.domain.notice.presentation.dto.response.NoticeDetailRes;
import com.ducami.dukkaebi.domain.notice.presentation.dto.response.NoticeListRes;
import com.ducami.dukkaebi.domain.notice.usecase.NoticeUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "공지 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NoticeController {
    private final NoticeUseCase noticeUseCase;

    @GetMapping
    @Operation(summary = "공지사항 모두 조회")
    public ResponseEntity<Page<NoticeListRes>> getNoticeList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<NoticeListRes> notices = noticeUseCase.getNoticeList(pageable);
        return ResponseEntity.ok(notices);
    }

    @GetMapping("/{noticeId}")
    @Operation(summary = "공지사항 상세 조회")
    public ResponseEntity<NoticeDetailRes> getNoticeDetail(@PathVariable Long noticeId) {
        NoticeDetailRes notice = noticeUseCase.getNoticeDetail(noticeId);
        return ResponseEntity.ok(notice);
    }

    @GetMapping("/search")
    @Operation(summary = "공지사항 검색")
    public ResponseEntity<List<NoticeListRes>> searchNotices(
            @RequestParam String keyword
    ) {
        List<NoticeListRes> notices = noticeUseCase.searchNotices(keyword);
        return ResponseEntity.ok(notices);
    }
}
