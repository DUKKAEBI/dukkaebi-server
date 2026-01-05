package com.ducami.dukkaebi.domain.course.domain;

import com.ducami.dukkaebi.domain.course.domain.enums.LevelType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@SuperBuilder
@Table(name = "tb_course")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tb_course_problem_ids", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "problem_id")
    private List<Long> problemIds;

    @ElementCollection
    @CollectionTable(name = "tb_course_participant_ids", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "user_id")
    private List<Long> participantIds;

    public void updateCourse(String title, String description, List<String> keywords, LevelType level) {
        this.title = title;
        this.description = description;
        this.keywords = keywords;
        this.level = level;
    }

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
