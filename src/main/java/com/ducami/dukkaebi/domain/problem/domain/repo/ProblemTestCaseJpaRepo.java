package com.ducami.dukkaebi.domain.problem.domain.repo;

import com.ducami.dukkaebi.domain.problem.domain.ProblemTestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProblemTestCaseJpaRepo extends JpaRepository<ProblemTestCase, Long> {
    List<ProblemTestCase> findByProblem_ProblemId(Long problemId);
}
