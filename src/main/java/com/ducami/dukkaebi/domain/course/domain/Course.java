package com.ducami.dukkaebi.domain.course.domain;

import com.ducami.dukkaebi.domain.course.domain.enums.LevelType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    @Id @Column(nullable = false, unique = true)
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
}
