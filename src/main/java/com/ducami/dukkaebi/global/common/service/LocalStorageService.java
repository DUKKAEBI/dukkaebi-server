package com.ducami.dukkaebi.global.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@Slf4j
@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    @Value("${server.domain}")
    private String serverDomain;

    @Value("${storage.local.root-path}")
    private String rootPath;

    @Value("${storage.local.public-base-path}")
    private String publicBasePath;

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        String filePath = createFileName(file.getOriginalFilename(), folder);
        Path root = getRootPath();
        Path path = resolveInsideRoot(root, filePath);

        try {
            Files.createDirectories(path.getParent());
            file.transferTo(path);
        } catch (IOException e) {
            log.error("로컬 파일 업로드 실패: {}", path, e);
            throw new RuntimeException("파일 업로드에 실패했습니다.", e);
        }

        return buildPublicUrl(filePath);
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }

        Path path = resolveInsideRoot(getRootPath(), extractFileNameFromUrl(fileUrl));
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("로컬 파일 삭제 실패: {}", path, e);
            throw new RuntimeException("파일 삭제에 실패했습니다.", e);
        }
    }

    @Override
    public String extractFileNameFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new IllegalArgumentException("파일 URL이 비어 있습니다.");
        }

        URI uri;
        try {
            uri = URI.create(fileUrl);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("올바르지 않은 파일 URL입니다.", e);
        }

        if (uri.isAbsolute() && !isServerUrl(uri)) {
            throw new IllegalArgumentException("로컬 저장소 파일 URL이 아닙니다.");
        }

        String path = uri.getPath();
        String prefix = normalizePath(publicBasePath) + "/";
        if (path == null || !path.startsWith(prefix)) {
            throw new IllegalArgumentException("로컬 저장소 파일 URL이 아닙니다.");
        }

        String filePath = path.substring(prefix.length());
        if (filePath.isBlank()) {
            throw new IllegalArgumentException("파일 경로가 비어 있습니다.");
        }
        return filePath;
    }

    private Path getRootPath() {
        return Paths.get(rootPath).toAbsolutePath().normalize();
    }

    private Path resolveInsideRoot(Path root, String filePath) {
        Path resolved = root.resolve(filePath).normalize();
        if (!resolved.startsWith(root)) {
            throw new IllegalArgumentException("허용되지 않은 파일 경로입니다.");
        }
        return resolved;
    }

    private String buildPublicUrl(String filePath) {
        return normalizeUrl(serverDomain) + normalizePath(publicBasePath) + "/" + filePath;
    }

    private boolean isServerUrl(URI uri) {
        URI configuredServerUri = URI.create(normalizeUrl(serverDomain));
        return Objects.equals(uri.getScheme(), configuredServerUri.getScheme())
                && Objects.equals(uri.getRawAuthority(), configuredServerUri.getRawAuthority());
    }

    private String normalizeUrl(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String normalizePath(String value) {
        return "/" + value.replaceAll("^/+|/+$", "");
    }
}
