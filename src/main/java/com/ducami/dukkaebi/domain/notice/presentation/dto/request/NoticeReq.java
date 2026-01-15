package com.ducami.dukkaebi.domain.notice.presentation.dto.request;

import com.ducami.dukkaebi.domain.notice.domain.Notice;
import com.ducami.dukkaebi.domain.user.domain.User;

public record NoticeReq(
        String title,
        String content,
        String fileUrl
) {
    public static Notice toEntity(NoticeReq req, User user) {
        return Notice.builder()
                .title(req.title)
                .content(req.content)
                .fileUrl(req.fileUrl)
                .writer(user.getNickname())
                .hits(0L)
                .build();
    }
}
