package com.ducami.dukkaebi.domain.contest.domain;

import com.ducami.dukkaebi.domain.contest.domain.enums.ContestStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@SuperBuilder
@Table(name = "tb_contest")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Contest {
    @Id @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContestStatus status;

    @ElementCollection
    @CollectionTable(name = "tb_contest_participant", joinColumns = @JoinColumn(name = "contest_id", referencedColumnName = "code"))
    private List<Long> participantIds;

    @ElementCollection
    @CollectionTable(name = "tb_contest_problem", joinColumns = @JoinColumn(name = "contest_id", referencedColumnName = "code"))
    private List<Long> problemIds;

    public void updateContest(String title, String description, LocalDate startDate, LocalDate endDate) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // 참가자 추가 (null 보호 + 중복 방지)
    public void addParticipant(Long userId) {
        if (participantIds == null) {
            participantIds = new ArrayList<>();
        }
        if (!participantIds.contains(userId)) {
            participantIds.add(userId);
        }
    }

    public boolean hasParticipant(Long userId) {
        return participantIds != null && participantIds.contains(userId);
    }

    // 대회 종료
    public void end() {
        this.status = ContestStatus.ENDED;
        this.endDate = LocalDate.now();
    }
}