package com.coursebuddy.domain.po;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "assignment_submissions")
public class AssignmentSubmissionPO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "assignment_id", nullable = false)
    private Long assignmentId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "submission_url", columnDefinition = "TEXT")
    private String submissionUrl;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    private Integer score;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "graded_at")
    private LocalDateTime gradedAt;

    @Column(name = "graded_by")
    private Long gradedBy;

    @Column(length = 16)
    @Builder.Default
    private String status = "SUBMITTED";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (submittedAt == null) submittedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
