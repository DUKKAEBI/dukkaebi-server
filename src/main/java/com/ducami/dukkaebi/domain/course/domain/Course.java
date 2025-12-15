package com.ducami.dukkaebi.domain.course.domain;

import com.ducami.dukkaebi.domain.course.domain.enums.LevelType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Entity
@SuperBuilder
@Table(name = "tb_course")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column
    private List<String> keywords;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LevelType level;

    @ElementCollection
    @CollectionTable(name = "tb_course_problem")
    private List<Long> problemIds;

    public void updateCourse(String title, String description, List<String> keywords, LevelType level) {
        this.title = title;
        this.description = description;
        this.keywords = keywords;
        this.level = level;
    }
}
