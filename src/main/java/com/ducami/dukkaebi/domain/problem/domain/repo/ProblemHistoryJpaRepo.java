package com.ducami.dukkaebi.domain.problem.domain.repo;

import com.ducami.dukkaebi.domain.problem.domain.ProblemHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProblemHistoryJpaRepo extends JpaRepository<ProblemHistory, Long> {
    ProblemHistory findByUser_Id(Long userId);
}
