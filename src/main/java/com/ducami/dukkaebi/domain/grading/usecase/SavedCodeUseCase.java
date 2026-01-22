package com.ducami.dukkaebi.domain.grading.usecase;

import com.ducami.dukkaebi.domain.grading.domain.SavedCode;
import com.ducami.dukkaebi.domain.grading.presentation.dto.request.CodeSaveReq;
import com.ducami.dukkaebi.domain.grading.presentation.dto.response.CodeSaveRes;
import com.ducami.dukkaebi.domain.grading.presentation.dto.response.SavedCodeRes;
import com.ducami.dukkaebi.domain.grading.service.SavedCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SavedCodeUseCase {
    private final SavedCodeService savedCodeService;

    public CodeSaveRes saveCode(CodeSaveReq request) {
        log.info("코드 저장 요청 - problemId: {}, language: {}", request.problemId(), request.language());

        // 기존 저장된 코드 확인
        Optional<SavedCode> existingCode = savedCodeService.getSavedCode(request.problemId());
        boolean isNewSave = existingCode.isEmpty();

        // 저장 또는 업데이트
        SavedCode savedCode = savedCodeService.saveOrUpdateCode(
                request.problemId(),
                request.code(),
                request.language()
        );

        log.info("코드 저장 완료 - problemId: {}, isNewSave: {}", request.problemId(), isNewSave);
        return CodeSaveRes.from(savedCode, isNewSave);
    }

    public SavedCodeRes getSavedCode(Long problemId) {
        log.info("저장된 코드 조회 - problemId: {}", problemId);

        Optional<SavedCode> savedCode = savedCodeService.getSavedCode(problemId);

        if (savedCode.isPresent()) {
            log.info("저장된 코드 발견 - problemId: {}, language: {}",
                    problemId, savedCode.get().getLanguage());
            return SavedCodeRes.from(savedCode.get());
        } else {
            log.info("저장된 코드 없음 - problemId: {}", problemId);
            return null;
        }
    }
}

