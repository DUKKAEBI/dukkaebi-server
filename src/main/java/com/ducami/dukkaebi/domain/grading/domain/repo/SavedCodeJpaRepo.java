package com.ducami.dukkaebi.domain.grading.domain.repo;

import com.ducami.dukkaebi.domain.grading.domain.SavedCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SavedCodeJpaRepo extends JpaRepository<SavedCode, Long> {
    Optional<SavedCode> findByUserIdAndProblemProblemId(Long userId, Long problemId);
    int deleteByUser_Id(Long userId);
    int deleteByProblem_ProblemId(Long problemId);
}

