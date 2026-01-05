package com.ducami.dukkaebi.domain.contest.domain.repo;

import com.ducami.dukkaebi.domain.contest.domain.ContestProblemScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContestProblemScoreJpaRepo extends JpaRepository<ContestProblemScore, Long> {
    List<ContestProblemScore> findByParticipant_Id(Long participantId);
    Optional<ContestProblemScore> findByParticipant_IdAndProblem_ProblemId(Long participantId, Long problemId);
}

