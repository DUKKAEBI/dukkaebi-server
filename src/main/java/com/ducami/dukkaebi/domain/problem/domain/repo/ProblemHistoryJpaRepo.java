package com.ducami.dukkaebi.domain.problem.domain.repo;

import com.ducami.dukkaebi.domain.problem.domain.ProblemHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProblemHistoryJpaRepo extends JpaRepository<ProblemHistory, Long> {
    List<ProblemHistory> findByUser_Id(Long userId);
}
