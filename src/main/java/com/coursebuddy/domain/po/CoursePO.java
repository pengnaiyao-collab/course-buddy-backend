package com.coursebuddy.domain.po;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "CoursePO")
@Table(name = "course_catalog")
public class CoursePO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 32)
    private String code;

    @Column(nullable = false, length = 256)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "instructor_id", nullable = false)
    private Long instructorId;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "credit_hours", nullable = false)
    @Builder.Default
    private Integer creditHours = 3;

    @Column(length = 16)
    @Builder.Default
    private String level = "BEGINNER";

    @Column(nullable = false)
    @Builder.Default
    private Integer capacity = 30;

    @Column(name = "enrolled_count", nullable = false)
    @Builder.Default
    private Integer enrolledCount = 0;

    @Column(name = "thumbnail_url", length = 512)
    private String thumbnailUrl;

    @Column(columnDefinition = "TEXT")
    private String syllabus;

    @Column(name = "max_grade", nullable = false)
    @Builder.Default
    private Integer maxGrade = 100;

    @Column(name = "passing_grade", nullable = false)
    @Builder.Default
    private Integer passingGrade = 60;

    @Column(length = 16)
    @Builder.Default
    private String status = "OPEN";

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

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
