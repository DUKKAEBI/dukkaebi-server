package com.ducami.dukkaebi.domain.grading.usecase;

import com.ducami.dukkaebi.domain.grading.error.JudgeErrorCode;
import com.ducami.dukkaebi.domain.grading.presentation.dto.request.CodeSubmitReq;
import com.ducami.dukkaebi.domain.grading.presentation.dto.request.CodeTestReq;
import com.ducami.dukkaebi.domain.grading.presentation.dto.response.JudgeResultRes;
import com.ducami.dukkaebi.domain.grading.service.JudgeService;
import com.ducami.dukkaebi.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JudgeUseCase {
    private final JudgeService judgeService;

    public JudgeResultRes submitCode(CodeSubmitReq request) {
        try {
            log.info("코드 제출 - problemId: {}, language: {}, timeSpent: {}초",
                    request.problemId(), request.language(), request.timeSpentSeconds());

            JudgeResultRes result = judgeService.judgeCode(
                    request.problemId(),
                    request.code(),
                    request.language(),
                    request.timeSpentSeconds()
            );

            log.info("채점 완료 - status: {}, passed: {}/{}",
                    result.status(), result.passedTestCases(), result.totalTestCases());

            return result;

        } catch (Exception e) {
            log.error("코드 제출 실패: {}", e.getMessage(), e);
            throw new CustomException(JudgeErrorCode.JUDGE_FAILED);
        }
    }

    public JudgeResultRes testCode(CodeTestReq request) {
        try {
            log.info("코드 테스트 - problemId: {}, language: {}",
                    request.problemId(), request.language());

            JudgeResultRes result = judgeService.testCode(
                    request.problemId(),
                    request.code(),
                    request.language()
            );

            log.info("테스트 완료 - status: {}, passed: {}/{}",
                    result.status(), result.passedTestCases(), result.totalTestCases());

            return result;

        } catch (Exception e) {
            log.error("코드 테스트 실패: {}", e.getMessage(), e);
            throw new CustomException(JudgeErrorCode.JUDGE_FAILED);
        }
    }
}
