package com.ducami.dukkaebi.domain.problem.domain.repo;

import com.ducami.dukkaebi.domain.problem.domain.Problem;
import com.ducami.dukkaebi.domain.problem.domain.enums.DifficultyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProblemJpaRepo extends JpaRepository<Problem, Long> {
    List<Problem> findByDifficulty(DifficultyType difficulty);

    List<Problem> searchByName(String name);
}
