package com.coursebuddy.domain.po;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "learning_tasks")
public class LearningTaskPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id")
    private Long courseId;

    @Column(nullable = false, length = 256)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 16)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(length = 16)
    @Builder.Default
    private String priority = "MEDIUM";

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
