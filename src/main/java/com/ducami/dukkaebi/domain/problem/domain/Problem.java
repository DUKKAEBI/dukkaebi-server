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

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String input;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String output;

    @Column
    @Enumerated(EnumType.STRING)
    private DifficultyType difficulty;  // 일반 문제용, 대회 문제는 null

    @Column(nullable = false)
    private Integer solvedCount;

    @Column(nullable = false)
    private Integer attemptCount;

    @Column(nullable = false)
    private LocalDate addedAt;

    @Column
    private String contestId;  // 대회 전용 문제의 대회 코드

    @Column
    private Integer score;  // 대회 전용 문제의 점수

    public void updateProblem(String name, String description, String input, String output, DifficultyType difficulty) {
        this.name = name;
        this.description = description;
        this.input = input;
        this.output = output;
        this.difficulty = difficulty;
    }

    public void updateContestProblem(String name, String description, String input, String output, Integer score) {
        this.name = name;
        this.description = description;
        this.input = input;
        this.output = output;
        this.score = score;
        this.difficulty = null;  // 대회 문제는 difficulty null
    }

    public void incrementSolvedCount() {
        this.solvedCount++;
    }

    public void incrementAttemptCount() {
        this.attemptCount++;
    }
}
