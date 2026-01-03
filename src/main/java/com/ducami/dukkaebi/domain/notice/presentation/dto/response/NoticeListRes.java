package com.ducami.dukkaebi.domain.notice.presentation.dto.response;

import com.ducami.dukkaebi.domain.notice.domain.Notice;

import java.time.LocalDate;

public record NoticeListRes(
        Long noticeId,
        String title,
        String writer,
        LocalDate date,
        Long hits
) {
    public static NoticeListRes from(Notice notice) {
        return new NoticeListRes(
                notice.getId(),
                notice.getTitle(),
                notice.getWriter(),
                notice.getCreatedAt().toLocalDate(),
                notice.getHits()
        );
    }
}
