package com.ducami.dukkaebi.domain.contest.domain.repo;

import com.ducami.dukkaebi.domain.contest.domain.Contest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContestJpaRepo extends JpaRepository<Contest, String> {
    boolean existsByTitle(String title);
    List<Contest> findAllByOrderByEndDateAsc();
    Page<Contest> findAllByOrderByEndDateAsc(Pageable pageable);
    List<Contest> findByTitleContainingIgnoreCaseOrderByEndDateAsc(String name);
    Page<Contest> findByTitleContainingIgnoreCaseOrderByEndDateAsc(String name, Pageable pageable);
}
