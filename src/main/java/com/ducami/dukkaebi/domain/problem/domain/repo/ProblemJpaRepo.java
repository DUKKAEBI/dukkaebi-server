package com.ducami.dukkaebi.domain.problem.domain.repo;

import com.ducami.dukkaebi.domain.problem.domain.Problem;
import com.ducami.dukkaebi.domain.problem.domain.enums.DifficultyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProblemJpaRepo extends JpaRepository<Problem, Long> {
    List<Problem> findByDifficulty(DifficultyType difficulty);

    @Query("SELECT p FROM Problem p WHERE p.name LIKE %:name%")
    List<Problem> searchByName(String name);

    // 일반 문제만 조회 (대회 전용 문제 제외)
    List<Problem> findByContestIdIsNull();

    // 특정 대회의 문제만 조회
    List<Problem> findByContestId(String contestId);
}
