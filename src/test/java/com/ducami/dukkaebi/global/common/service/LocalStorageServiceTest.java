package com.ducami.dukkaebi.global.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalStorageServiceTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void uploadsAndDeletesFileUsingPublicUrl() throws Exception {
        LocalStorageService service = createService();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample.txt",
                "text/plain",
                "hello".getBytes()
        );

        String fileUrl = service.uploadFile(file, "notice");
        String filePath = service.extractFileNameFromUrl(fileUrl);
        Path savedFile = temporaryDirectory.resolve(filePath);

        assertThat(fileUrl).startsWith("http://localhost:8080/files/notice/");
        assertThat(savedFile).exists().hasContent("hello");

        service.deleteFile(fileUrl);

        assertThat(savedFile).doesNotExist();
    }

    @Test
    void rejectsUrlOutsideLocalStoragePath() {
        LocalStorageService service = createService();

        assertThatThrownBy(() -> service.extractFileNameFromUrl("http://localhost:8080/other/file.txt"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("로컬 저장소 파일 URL이 아닙니다.");
    }

    @Test
    void rejectsUrlFromAnotherServer() {
        LocalStorageService service = createService();

        assertThatThrownBy(() -> service.extractFileNameFromUrl("https://other.example.com/files/notice/file.txt"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("로컬 저장소 파일 URL이 아닙니다.");
    }

    private LocalStorageService createService() {
        LocalStorageService service = new LocalStorageService();
        ReflectionTestUtils.setField(service, "serverDomain", "http://localhost:8080/");
        ReflectionTestUtils.setField(service, "rootPath", temporaryDirectory.toString());
        ReflectionTestUtils.setField(service, "publicBasePath", "/files/");
        return service;
    }
}
