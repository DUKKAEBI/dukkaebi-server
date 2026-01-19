package com.ducami.dukkaebi.domain.contest.domain;

import com.ducami.dukkaebi.domain.problem.domain.Problem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@Entity
@SuperBuilder
@Table(name = "tb_contest_problem_mapping")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContestProblemMapping {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_id", nullable = false, referencedColumnName = "code")
    private Contest contest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(nullable = false)
    private Integer score;

    public void updateScore(Integer score) {
        this.score = score;
    }
}

