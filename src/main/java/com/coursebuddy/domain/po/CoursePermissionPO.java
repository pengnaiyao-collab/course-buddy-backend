package com.coursebuddy.domain.po;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 课程权限 PO - 记录用户在特定课程中的权限级别
 * L1: 课程库管理员（最高权限）
 * L2: 核心协作成员
 * L3: 选课班级成员
 * L4: 校内访客成员（只读）
 */
@Entity
@Table(name = "course_permissions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "course_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoursePermissionPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    /**
     * 权限级别: L1, L2, L3, L4
     */
    @Column(name = "permission_level", nullable = false, length = 2)
    private String permissionLevel;

    @Column(name = "granted_by")
    private Long grantedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
