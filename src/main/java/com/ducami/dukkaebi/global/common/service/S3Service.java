package com.ducami.dukkaebi.global.common.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "storage.provider", havingValue = "s3")
public class S3Service implements StorageService {
    private final AmazonS3Client amazonS3Client;

    private static final String AMAZON_AWS_DOMAIN = ".amazonaws.com";

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    /**
     * S3에 파일 업로드
     * @param file 업로드할 파일
     * @param folder S3 내 폴더 경로 (예: "contest", "notice")
     * @return 업로드된 파일의 URL
     */
    @Override
    public String uploadFile(MultipartFile file, String folder) {
        String fileName = createFileName(file.getOriginalFilename(), folder);

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(file.getSize());
        objectMetadata.setContentType(file.getContentType());

        try (InputStream inputStream = file.getInputStream()) {
            amazonS3Client.putObject(new PutObjectRequest(bucketName, fileName, inputStream, objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (IOException e) {
            log.error("S3 파일 업로드 실패: {}", e.getMessage());
            throw new RuntimeException("파일 업로드에 실패했습니다.", e);
        }

        return amazonS3Client.getUrl(bucketName, fileName).toString();
    }

    /**
     * S3에서 파일 삭제
     * @param fileUrl 삭제할 파일의 URL
     */
    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            String fileName = extractFileNameFromUrl(fileUrl);
            amazonS3Client.deleteObject(new DeleteObjectRequest(bucketName, fileName));
            log.info("S3 파일 삭제 성공: {}", fileName);
        } catch (Exception e) {
            log.error("S3 파일 삭제 실패: {}", e.getMessage());
            throw new RuntimeException("파일 삭제에 실패했습니다.", e);
        }
    }

    @Override
    public String extractFileNameFromUrl(String fileUrl) {
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

    private boolean isVirtualHostedStyle(String host, String bucketName) {
        return host.startsWith(bucketName + ".s3.") && host.endsWith(AMAZON_AWS_DOMAIN);
    }

    private boolean isPathStyle(String host, String bucketName) {
        return host.startsWith("s3.") && host.endsWith(AMAZON_AWS_DOMAIN) && bucketName != null && !bucketName.isBlank();
    }

    private String trimLeadingSlash(String path) {
        return path.startsWith("/") ? path.substring(1) : path;
    }
}
