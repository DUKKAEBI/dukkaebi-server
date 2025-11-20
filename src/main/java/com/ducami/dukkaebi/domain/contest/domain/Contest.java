package com.ducami.dukkaebi.domain.contest.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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

    @ElementCollection
    @CollectionTable(name = "tb_contest_participant")
    private List<Long> participantIds;

    @ElementCollection
    @CollectionTable(name = "tb_contest_problem")
    private List<Long> problemIds;

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
}