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
@Table(name = "content_reviews")
public class ContentReviewPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Column(name = "reviewer_id", nullable = false)
    private Long reviewerId;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "PENDING";

    @Column(length = 500)
    private String comments;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
