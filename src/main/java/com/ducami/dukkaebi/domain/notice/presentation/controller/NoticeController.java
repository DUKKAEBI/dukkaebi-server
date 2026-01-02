package com.ducami.dukkaebi.domain.notice.presentation.controller;

import com.ducami.dukkaebi.domain.notice.presentation.dto.response.NoticeDetailRes;
import com.ducami.dukkaebi.domain.notice.presentation.dto.response.NoticeListRes;
import com.ducami.dukkaebi.domain.notice.usecase.NoticeUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@Tag(name = "공지 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("notice")
public class NoticeController {
    private final NoticeUseCase noticeUseCase;

    @GetMapping
    public ResponseEntity<Page<NoticeListRes>> getNoticeList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<NoticeListRes> notices = noticeUseCase.getNoticeList(pageable);
        return ResponseEntity.ok(notices);
    }

    @GetMapping("/{noticeId}")
    public ResponseEntity<NoticeDetailRes> getNoticeDetail(@PathVariable Long noticeId) {
        NoticeDetailRes notice = noticeUseCase.getNoticeDetail(noticeId);
        return ResponseEntity.ok(notice);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<NoticeListRes>> searchNotices(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<NoticeListRes> notices = noticeUseCase.searchNotices(keyword, pageable);
        return ResponseEntity.ok(notices);
    }
}
