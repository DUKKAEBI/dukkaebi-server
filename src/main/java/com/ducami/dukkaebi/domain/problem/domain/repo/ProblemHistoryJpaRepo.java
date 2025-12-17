package com.ducami.dukkaebi.domain.problem.domain.repo;

import com.ducami.dukkaebi.domain.problem.domain.ProblemHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProblemHistoryJpaRepo extends JpaRepository<ProblemHistory, Long> {
    List<ProblemHistory> findByUser_Id(Long userId);
    int deleteByUser_Id(Long userId);

    // 사용자-문제 단건 조회
    Optional<ProblemHistory> findByUser_IdAndProblem_ProblemId(Long userId, Long problemId);
}
