package com.ducami.dukkaebi.domain.notice.presentation.controller;

import com.ducami.dukkaebi.domain.notice.presentation.dto.response.NoticeDetailRes;
import com.ducami.dukkaebi.domain.notice.presentation.dto.response.NoticeHomeRes;
import com.ducami.dukkaebi.domain.notice.presentation.dto.response.NoticeListRes;
import com.ducami.dukkaebi.domain.notice.usecase.NoticeUseCase;
import com.ducami.dukkaebi.global.common.dto.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
    public PageResponse<NoticeListRes> getNoticeList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        return noticeUseCase.getNoticeListPaged(page, size);
    }

    @GetMapping("/home")
    @Operation(summary = "홈화면 최신 공지사항 5개 조회")
    public List<NoticeHomeRes> getRecentNoticesForHome() {
        return noticeUseCase.getRecentNoticesForHome();
    }

    @GetMapping("/{noticeId}")
    @Operation(summary = "공지사항 상세 조회")
    public NoticeDetailRes getNoticeDetail(@PathVariable Long noticeId) {
        return noticeUseCase.getNoticeDetail(noticeId);
    }

    @GetMapping("/search")
    @Operation(summary = "공지사항 검색")
    public List<NoticeListRes> searchNotices(@RequestParam String keyword) {
        return noticeUseCase.searchNotices(keyword);
    }
}
