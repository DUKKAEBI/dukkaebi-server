package com.ducami.dukkaebi.domain.contest.domain.repo;

import com.ducami.dukkaebi.domain.contest.domain.ContestProblemMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContestProblemMappingJpaRepo extends JpaRepository<ContestProblemMapping, Long> {
    List<ContestProblemMapping> findByContest_Code(String contestCode);
    Optional<ContestProblemMapping> findByContest_CodeAndProblem_ProblemId(String contestCode, Long problemId);
    int deleteByContest_Code(String contestCode);
    int deleteByContest_CodeAndProblem_ProblemId(String contestCode, Long problemId);
}

