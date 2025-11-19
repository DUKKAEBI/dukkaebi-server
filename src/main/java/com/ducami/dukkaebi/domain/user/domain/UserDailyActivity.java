package com.ducami.dukkaebi.domain.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@Entity
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tb_user_daily_activity",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_date", columnNames = {"userId", "activityDate"}))
public class UserDailyActivity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate activityDate;

    @Column(nullable = false)
    private int solvedCount;

    public void increaseSolvedCount(int delta) {
        this.solvedCount += delta;
    }
}

