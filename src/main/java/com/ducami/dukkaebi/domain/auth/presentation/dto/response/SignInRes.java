package com.ducami.dukkaebi.domain.auth.presentation.dto.response;

import lombok.Builder;

@Builder
public record SignInRes(String refreshToken, String accessToken) {
}
