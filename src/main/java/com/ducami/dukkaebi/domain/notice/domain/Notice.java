package com.ducami.dukkaebi.domain.notice.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Entity
@SuperBuilder
@Table(name = "tb_notice")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column
    private String fileUrl;

    @Column(nullable = false)
    private String writer;

    @Column(nullable = false)
    private Long hits;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void incrementViewCount() {
        this.hits++;
    }

    public void updateNotice(String title, String content, String fileUrl) {
        this.title = title;
        this.content = content;
        this.fileUrl = fileUrl;
    }
}
