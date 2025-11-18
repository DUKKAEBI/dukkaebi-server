package com.ducami.dukkaebi.domain.problem.domain;

import com.ducami.dukkaebi.domain.problem.domain.enums.SolvedResult;
import com.ducami.dukkaebi.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@Entity
@SuperBuilder
@Table(name = "tb_problem_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProblemHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long problemHistoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problemId")
    private Problem problem;

    @Enumerated(EnumType.STRING)
    @Column(name = "sovledResult")
    private SolvedResult solvedResult;
}
