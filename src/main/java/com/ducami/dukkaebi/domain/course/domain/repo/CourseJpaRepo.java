package com.ducami.dukkaebi.domain.course.domain.repo;

import com.ducami.dukkaebi.domain.course.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseJpaRepo extends JpaRepository<Course, Long> {
    boolean existsByTitle(String title);

}
