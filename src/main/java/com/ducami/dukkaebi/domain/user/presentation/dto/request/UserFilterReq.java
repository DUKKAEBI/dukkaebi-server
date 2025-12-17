package com.ducami.dukkaebi.domain.user.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserFilterReq(
        @Schema(description = "검색 키워드 (닉네임 또는 로그인 아이디)")
        String keyword,

        @Schema(description = "정렬 기준 (NICKNAME, LOGIN_ID, GROWTH)", example = "NICKNAME")
        SortType sortBy
) {
    public enum SortType {
        NICKNAME,
        LOGIN_ID,
        GROWTH
    }

    // 기본값 설정
    public UserFilterReq {
        if (sortBy == null) {
            sortBy = SortType.NICKNAME;
        }
    }
}
