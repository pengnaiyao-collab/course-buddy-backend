package com.coursebuddy.domain.po;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 笔记持久化对象。
 *
 * <p>使用 JPA 乐观锁（{@code @Version}）防止并发更新冲突。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notes")
public class NotePO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 乐观锁版本号，由 JPA 自动维护。
     */
    @Version
    @Column(name = "opt_lock_version", nullable = false)
    @Builder.Default
    private Long optLockVersion = 0L;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id")
    private Long courseId;

    /** 所属分类 ID（外键，可选）。 */
    @Column(name = "category_id")
    private Long categoryId;

    @Column(nullable = false, length = 256)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 笔记摘要/描述（可选）。 */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** 笔记状态：DRAFT（草稿）、PUBLISHED（已发布）、ARCHIVED（已归档）。 */
    @Column(length = 16, nullable = false)
    @Builder.Default
    private String status = "DRAFT";

    @Column(length = 64)
    private String category;

    @Column(length = 256)
    private String tags;

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
