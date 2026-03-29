package com.coursebuddy.domain.po;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "course_resources")
public class CourseResourcePO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(nullable = false, length = 256)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "resource_type", length = 16)
    @Builder.Default
    private String resourceType = "OTHER";

    @Column(name = "resource_url", nullable = false, columnDefinition = "TEXT")
    private String resourceUrl;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "download_count")
    @Builder.Default
    private Integer downloadCount = 0;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

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
