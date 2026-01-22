package com.ducami.dukkaebi.domain.grading.domain;

import com.ducami.dukkaebi.domain.problem.domain.Problem;
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
@Table(name = "tb_saved_code",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "problem_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SavedCode {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String code;

    @Column(nullable = false)
    private String language;  // "java", "python", "cpp"

    @Column(nullable = false)
    private LocalDateTime savedAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 코드 업데이트 메서드
    public void updateCode(String code, String language) {
        this.code = code;
        this.language = language;
        this.updatedAt = LocalDateTime.now();
    }
}

