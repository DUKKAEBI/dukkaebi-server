package com.ducami.dukkaebi.global.common.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

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
        S3Service service = createService();

        assertThat(service.extractFileNameFromUrl(fileUrl)).isEqualTo("notice/image.png");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://other-bucket.s3.ap-northeast-2.amazonaws.com/notice/image.png",
            "https://example.com/dukkaebi-assets/notice/image.png"
    })
    void rejectsUrlOutsideConfiguredBucket(String fileUrl) {
        S3Service service = createService();

        assertThatThrownBy(() -> service.extractFileNameFromUrl(fileUrl))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private S3Service createService() {
        S3Service service = new S3Service(null);
        ReflectionTestUtils.setField(service, "bucketName", BUCKET_NAME);
        return service;
    }
}
