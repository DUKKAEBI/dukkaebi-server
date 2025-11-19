package com.ducami.dukkaebi.domain.grading.util;

import com.ducami.dukkaebi.domain.grading.model.ExecutionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.*;

@Slf4j
@Component
public class CodeExecutor {
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

    /**
     * 코드 실행 (현재 Java만 지원, Python/C++는 확장 가능)
     */
    public ExecutionResult execute(String code, String language, String input, long timeoutMs) {
        log.info("코드 실행 시작 - language: {}", language);

        return switch (language.toLowerCase()) {
            case "java" -> executeJava(code, input, timeoutMs);
            case "python", "python3" -> executePython(code, input, timeoutMs);
            case "cpp", "c++" -> executeCpp(code, input, timeoutMs);
            default -> new ExecutionResult("", "지원하지 않는 언어입니다: " + language, false, false);
        };
    }

    /**
     * Java 코드 실행
     */
    private ExecutionResult executeJava(String code, String input, long timeoutMs) {
        Path tempDir = null;

        try {
            code = normalizeCode(code);

            // 1. 임시 디렉토리 생성
            tempDir = Files.createTempDirectory("judge_");

            // 2. Main.java 파일 생성
            Path javaFile = tempDir.resolve("Main.java");
            Files.writeString(javaFile, code, StandardCharsets.UTF_8);

            // 3. 컴파일
            ProcessBuilder compileBuilder = new ProcessBuilder(
                    "javac", "-encoding", "UTF-8", javaFile.toString()
            );
            compileBuilder.directory(tempDir.toFile());
            Process compileProcess = compileBuilder.start();

            boolean compileFinished = compileProcess.waitFor(10, TimeUnit.SECONDS);

            if (!compileFinished || compileProcess.exitValue() != 0) {
                String compileError = readStream(compileProcess.getErrorStream());
                return new ExecutionResult("", "컴파일 에러: " + compileError, false, false);
            }

            // 4. 실행
            ProcessBuilder runBuilder = new ProcessBuilder("java", "-cp", tempDir.toString(), "Main");
            runBuilder.directory(tempDir.toFile());
            Process runProcess = runBuilder.start();

            // 5. 입력 전달
            try (OutputStream os = runProcess.getOutputStream()) {
                os.write(input.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            // 6. 타임아웃과 함께 결과 대기
            boolean finished = runProcess.waitFor(timeoutMs, TimeUnit.MILLISECONDS);

            if (!finished) {
                runProcess.destroyForcibly();
                return new ExecutionResult("", "시간 초과", false, true);
            }

            // 7. 출력 읽기
            String output = readStream(runProcess.getInputStream());
            String error = readStream(runProcess.getErrorStream());

            boolean success = runProcess.exitValue() == 0;

            return new ExecutionResult(output, error, success, false);

        } catch (Exception e) {
            log.error("Java 실행 중 예외: {}", e.getMessage(), e);
            return new ExecutionResult("", "실행 에러: " + e.getMessage(), false, false);

        } finally {
            // 임시 파일 정리
            if (tempDir != null) {
                deleteDirectory(tempDir.toFile());
            }
        }
    }

    /**
     * Python 코드 실행
     */
    private ExecutionResult executePython(String code, String input, long timeoutMs) {
        File tempFile = null;

        try {
            code = normalizeCode(code);

            // 1. 임시 파일 생성
            tempFile = File.createTempFile("judge_", ".py");
            Files.writeString(tempFile.toPath(), code, StandardCharsets.UTF_8);

            // 2. 실행
            ProcessBuilder pb = new ProcessBuilder("python3", tempFile.getAbsolutePath());
            Process process = pb.start();

            // 3. 입력 전달
            try (OutputStream os = process.getOutputStream()) {
                os.write(input.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            // 4. 타임아웃과 함께 결과 대기
            boolean finished = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);

            if (!finished) {
                process.destroyForcibly();
                return new ExecutionResult("", "시간 초과", false, true);
            }

            // 5. 출력 읽기
            String output = readStream(process.getInputStream());
            String error = readStream(process.getErrorStream());

            return new ExecutionResult(output, error, process.exitValue() == 0, false);

        } catch (Exception e) {
            log.error("Python 실행 중 예외: {}", e.getMessage(), e);
            return new ExecutionResult("", "실행 에러: " + e.getMessage(), false, false);

        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
    }

    /**
     * C++ 코드 실행
     */
    private ExecutionResult executeCpp(String code, String input, long timeoutMs) {
        Path tempDir = null;

        try {
            code = normalizeCode(code);

            // 1. 임시 디렉토리 생성
            tempDir = Files.createTempDirectory("judge_");

            // 2. main.cpp 파일 생성
            Path cppFile = tempDir.resolve("main.cpp");
            Files.writeString(cppFile, code, StandardCharsets.UTF_8);

            // 3. 컴파일
            Path exeFile = tempDir.resolve("main");
            ProcessBuilder compileBuilder = new ProcessBuilder(
                    "g++", "-o", exeFile.toString(), cppFile.toString(), "-std=c++17"
            );
            compileBuilder.directory(tempDir.toFile());
            Process compileProcess = compileBuilder.start();

            boolean compileFinished = compileProcess.waitFor(10, TimeUnit.SECONDS);

            if (!compileFinished || compileProcess.exitValue() != 0) {
                String compileError = readStream(compileProcess.getErrorStream());
                return new ExecutionResult("", "컴파일 에러: " + compileError, false, false);
            }

            // 4. 실행
            ProcessBuilder runBuilder = new ProcessBuilder(exeFile.toString());
            runBuilder.directory(tempDir.toFile());
            Process runProcess = runBuilder.start();

            // 5. 입력 전달
            try (OutputStream os = runProcess.getOutputStream()) {
                os.write(input.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            // 6. 타임아웃과 함께 결과 대기
            boolean finished = runProcess.waitFor(timeoutMs, TimeUnit.MILLISECONDS);

            if (!finished) {
                runProcess.destroyForcibly();
                return new ExecutionResult("", "시간 초과", false, true);
            }

            // 7. 출력 읽기
            String output = readStream(runProcess.getInputStream());
            String error = readStream(runProcess.getErrorStream());

            boolean success = runProcess.exitValue() == 0;

            return new ExecutionResult(output, error, success, false);

        } catch (Exception e) {
            log.error("C++ 실행 중 예외: {}", e.getMessage(), e);
            return new ExecutionResult("", "실행 에러: " + e.getMessage(), false, false);

        } finally {
            // 임시 파일 정리
            if (tempDir != null) {
                deleteDirectory(tempDir.toFile());
            }
        }
    }

    /**
     * InputStream을 String으로 변환
     */
    private String readStream(InputStream is) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }

    /**
     * 디렉토리 삭제
     */
    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }

    /**
     * 코드 문자열 정규화 (이스케이프 시퀀스 처리)
     */
    private String normalizeCode(String code) {
        if (code == null) return "";

        return code
                .replace("\\n", "\n")      // \n을 실제 줄바꿈으로
                .replace("\\t", "\t")      // \t를 실제 탭으로
                .replace("\\r", "\r")      // \r을 실제 캐리지 리턴으로
                .replace("\\\"", "\"")     // \" 처리
                .replace("\\\\'", "'");    // \' 처리
    }
}

