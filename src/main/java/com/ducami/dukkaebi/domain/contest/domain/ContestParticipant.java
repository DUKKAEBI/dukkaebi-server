package com.ducami.dukkaebi.domain.contest.domain;

import com.ducami.dukkaebi.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Entity
@SuperBuilder
@Table(name = "tb_contest_participant")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContestParticipant {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_id", nullable = false, referencedColumnName = "code")
    private Contest contest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer totalScore;

    @Column(nullable = false)
    private Integer totalTimeSeconds; // 소요 시간 (초 단위)

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    public void updateTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public void updateTotalTime(Integer totalTimeSeconds) {
        this.totalTimeSeconds = totalTimeSeconds;
    }
}

