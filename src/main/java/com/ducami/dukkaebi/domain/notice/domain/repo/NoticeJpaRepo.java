package com.ducami.dukkaebi.domain.notice.domain.repo;

import com.ducami.dukkaebi.domain.notice.domain.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoticeJpaRepo extends JpaRepository<Notice, Long> {
    @Query("SELECT n FROM Notice n WHERE n.title LIKE %:keyword%")
    List<Notice> searchByKeyword(@Param("keyword") String keyword);
}
