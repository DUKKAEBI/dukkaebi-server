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
@Table(name = "tb_contest_problem_score")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContestProblemScore {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private ContestParticipant participant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(nullable = false)
    private Integer earnedScore; // 받은 점수

    @Column(nullable = false)
    private Integer timeSpentSeconds; // 해당 문제에 소요한 시간 (초)

    public void updateScore(Integer earnedScore) {
        this.earnedScore = earnedScore;
    }
}

