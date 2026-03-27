package com.coursebuddy.domain.po;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ai_generated_contents")
public class GeneratedContentPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 内容类型: OUTLINE, EXAM_POINTS, QUESTIONS, BREAKDOWN */
    @Column(name = "content_type", length = 32, nullable = false)
    private String contentType;

    @Column(columnDefinition = "TEXT")
    private String prompt;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "course_id")
    private Long courseId;

    @Column(length = 128)
    private String subject;

    /** 状态: PENDING, COMPLETED, FAILED */
    @Column(length = 16)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "token_count")
    private Integer tokenCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
