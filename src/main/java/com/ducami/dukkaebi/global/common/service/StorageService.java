package com.ducami.dukkaebi.global.common.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface StorageService {
    String uploadFile(MultipartFile file, String folder);

    void deleteFile(String fileUrl);

    /**
     * 고유한 파일명 생성
     * @param originalFilename 원본 파일명
     * @param folder 폴더 경로
     * @return UUID가 포함된 고유 파일명
     */
    default String createFileName(String originalFilename, String folder) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return folder + "/" + UUID.randomUUID() + extension;
    }

    String extractFileNameFromUrl(String fileUrl);
}
