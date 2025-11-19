package com.ducami.dukkaebi.domain.contest.domain.repo;

import com.ducami.dukkaebi.domain.contest.domain.Contest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContestJpaRepo extends JpaRepository<Contest, String> {
    boolean existsByCode(String code);
    boolean existsByTitle(String title);
    List<Contest> findAllByOrderByEndDateAsc();
}
