package com.ducami.dukkaebi.global.common.service;

import com.ducami.dukkaebi.global.common.util.S3UrlUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class S3ServiceTest {
    private static final String BUCKET_NAME = "dukkaebi-assets";

    @ParameterizedTest
    @ValueSource(strings = {
            "https://dukkaebi-assets.s3.ap-northeast-2.amazonaws.com/notice/image.png",
            "https://s3.ap-northeast-2.amazonaws.com/dukkaebi-assets/notice/image.png"
    })
    void extractsKeyFromSupportedS3UrlStyles(String fileUrl) {
        assertThat(S3UrlUtil.extractFileNameFromUrl(fileUrl, BUCKET_NAME)).isEqualTo("notice/image.png");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://other-bucket.s3.ap-northeast-2.amazonaws.com/notice/image.png",
            "https://example.com/dukkaebi-assets/notice/image.png"
    })
    void rejectsUrlOutsideConfiguredBucket(String fileUrl) {
        assertThatThrownBy(() -> S3UrlUtil.extractFileNameFromUrl(fileUrl, BUCKET_NAME))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
