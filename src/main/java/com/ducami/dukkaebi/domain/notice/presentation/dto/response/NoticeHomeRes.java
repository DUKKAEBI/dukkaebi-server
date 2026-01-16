package com.ducami.dukkaebi.domain.notice.presentation.dto.response;

import com.ducami.dukkaebi.domain.notice.domain.Notice;

import java.time.LocalDate;

public record NoticeHomeRes(
        Long noticeId,
        String title,
        String content,
        String writer,
        LocalDate date,
        String fileUrl
) {
    public static NoticeHomeRes from(Notice notice) {
        return new NoticeHomeRes(
                notice.getId(),
                notice.getTitle(),
                notice.getContent(),
                notice.getWriter(),
                notice.getCreatedAt().toLocalDate(),
                notice.getFileUrl()
        );
    }
}

