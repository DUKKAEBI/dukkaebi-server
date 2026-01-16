package com.ducami.dukkaebi.domain.contest.domain.repo;

import com.ducami.dukkaebi.domain.contest.domain.ContestParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContestParticipantJpaRepo extends JpaRepository<ContestParticipant, Long> {
    List<ContestParticipant> findByContest_CodeOrderByTotalScoreDescTotalTimeSecondsAsc(String contestCode);
    Optional<ContestParticipant> findByContest_CodeAndUser_Id(String contestCode, Long userId);
    boolean existsByContest_CodeAndUser_Id(String contestCode, Long userId);
    List<ContestParticipant> findByUser_Id(Long userId);
    int deleteByUser_Id(Long userId);
}

