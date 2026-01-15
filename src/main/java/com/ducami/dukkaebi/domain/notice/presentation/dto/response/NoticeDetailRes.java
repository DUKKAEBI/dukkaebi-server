package com.ducami.dukkaebi.domain.notice.presentation.dto.response;

import com.ducami.dukkaebi.domain.notice.domain.Notice;

import java.time.LocalDate;

public record NoticeDetailRes(
        String title,
        String writer,
        String content,
        String fileUrl,
        LocalDate createdAt
){
    public static NoticeDetailRes from(Notice notice) {
        return new NoticeDetailRes(
                notice.getTitle(),
                notice.getWriter(),
                notice.getContent(),
                notice.getFileUrl(),
                notice.getCreatedAt().toLocalDate()
        );
    }
}
