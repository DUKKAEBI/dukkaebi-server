package com.ducami.dukkaebi.domain.auth.presentation.dto.response;

import lombok.Builder;

@Builder
public record RefreshRes(String accessToken) {
}
