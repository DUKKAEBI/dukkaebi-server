package com.ducami.dukkaebi.domain.problem.domain;

import com.ducami.dukkaebi.domain.problem.domain.enums.DifficultyType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@Entity
@SuperBuilder
@Table(name = "tb_problem")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Problem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long problemId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DifficultyType difficulty;

    @Column(nullable = false)
    private Integer solvedCount = 0;

    @Column(nullable = false)
    private Integer attemptCount = 0;

    @Column(nullable = false)
    private LocalDate addedAt = LocalDate.now();
}
