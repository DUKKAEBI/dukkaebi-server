package com.ducami.dukkaebi.domain.notice.domain.repo;

import com.ducami.dukkaebi.domain.notice.domain.Notice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoticeJpaRepo extends JpaRepository<Notice, Long> {
    @Query("SELECT n FROM Notice n " +
            "WHERE (:keyword IS NULL OR n.title LIKE %:keyword% OR n.content LIKE %:keyword%) " +
            "ORDER BY n.createdAt DESC")
    Page<Notice> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
