package com.ducami.dukkaebi.domain.grading.service;

import com.ducami.dukkaebi.domain.grading.domain.SavedCode;
import com.ducami.dukkaebi.domain.grading.domain.repo.SavedCodeJpaRepo;
import com.ducami.dukkaebi.domain.problem.domain.Problem;
import com.ducami.dukkaebi.domain.problem.domain.repo.ProblemJpaRepo;
import com.ducami.dukkaebi.domain.problem.error.ProblemErrorCode;
import com.ducami.dukkaebi.domain.user.domain.User;
import com.ducami.dukkaebi.global.exception.CustomException;
import com.ducami.dukkaebi.global.security.auth.UserSessionHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SavedCodeService {
    private final SavedCodeJpaRepo savedCodeJpaRepo;
    private final ProblemJpaRepo problemJpaRepo;
    private final UserSessionHolder userSessionHolder;

    @Transactional
    public SavedCode saveOrUpdateCode(Long problemId, String code, String language) {
        User currentUser = userSessionHolder.getUser();
        Problem problem = problemJpaRepo.findById(problemId)
                .orElseThrow(() -> new CustomException(ProblemErrorCode.PROBLEM_NOT_FOUND));

        // 기존에 저장된 코드가 있는지 확인
        Optional<SavedCode> existingSavedCode = savedCodeJpaRepo
                .findByUserIdAndProblemProblemId(currentUser.getId(), problemId);

        if (existingSavedCode.isPresent()) {
            // 기존 코드 업데이트
            SavedCode savedCode = existingSavedCode.get();
            savedCode.updateCode(code, language);
            log.info("코드 업데이트 - userId: {}, problemId: {}, language: {}",
                    currentUser.getId(), problemId, language);
            return savedCodeJpaRepo.save(savedCode);
        } else {
            // 새로운 코드 저장
            LocalDateTime now = LocalDateTime.now();
            SavedCode newSavedCode = SavedCode.builder()
                    .user(currentUser)
                    .problem(problem)
                    .code(code)
                    .language(language)
                    .savedAt(now)
                    .updatedAt(now)
                    .build();
            log.info("새 코드 저장 - userId: {}, problemId: {}, language: {}",
                    currentUser.getId(), problemId, language);
            return savedCodeJpaRepo.save(newSavedCode);
        }
    }

    @Transactional(readOnly = true)
    public Optional<SavedCode> getSavedCode(Long problemId) {
        Long currentUserId = userSessionHolder.getUserId();
        return savedCodeJpaRepo.findByUserIdAndProblemProblemId(currentUserId, problemId);
    }
}

