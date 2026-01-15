package com.ducami.dukkaebi.domain.course.domain.repo;

import com.ducami.dukkaebi.domain.course.domain.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseJpaRepo extends JpaRepository<Course, Long> {
    boolean existsByTitle(String title);
    List<Course> findByTitleContainingIgnoreCase(String name);
    Page<Course> findByTitleContainingIgnoreCase(String name, Pageable pageable);
    Page<Course> findAll(Pageable pageable);
}
