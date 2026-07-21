package com.ducami.dukkaebi.global.common.util;

import java.net.URI;

public final class S3UrlUtil {

    private static final String AMAZON_AWS_DOMAIN = ".amazonaws.com";

    private S3UrlUtil() {
    }

    public static String extractFileNameFromUrl(String fileUrl, String bucketName) {
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new IllegalArgumentException("S3 URL은 비어 있을 수 없습니다.");
        }

        URI uri = URI.create(fileUrl);
        String host = uri.getHost();
        String path = uri.getPath();

        if (host == null || path == null) {
            throw new IllegalArgumentException("유효하지 않은 S3 URL입니다.");
        }

        if (isVirtualHostedStyle(host, bucketName)) {
            return trimLeadingSlash(path);
        }

        if (isPathStyle(host, bucketName)) {
            String prefix = "/" + bucketName + "/";
            if (!path.startsWith(prefix)) {
                throw new IllegalArgumentException("요청한 버킷과 URL이 일치하지 않습니다.");
            }
            return path.substring(prefix.length());
        }

        throw new IllegalArgumentException("요청한 버킷과 URL이 일치하지 않습니다.");
    }

    private static boolean isVirtualHostedStyle(String host, String bucketName) {
        return host.startsWith(bucketName + ".s3.") && host.endsWith(AMAZON_AWS_DOMAIN);
    }

    private static boolean isPathStyle(String host, String bucketName) {
        return host.startsWith("s3.") && host.endsWith(AMAZON_AWS_DOMAIN)
                && !bucketName.isBlank();
    }

    private static String trimLeadingSlash(String path) {
        return path.startsWith("/") ? path.substring(1) : path;
    }
}
