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
@Table(name = "user_settings")
public class UserSettingsPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "notify_email", nullable = false)
    @Builder.Default
    private Boolean notifyEmail = true;

    @Column(name = "notify_push", nullable = false)
    @Builder.Default
    private Boolean notifyPush = true;

    @Column(name = "privacy_profile", nullable = false, length = 16)
    @Builder.Default
    private String privacyProfile = "PUBLIC";

    @Column(nullable = false, length = 8)
    @Builder.Default
    private String language = "zh_CN";

    @Column(nullable = false, length = 16)
    @Builder.Default
    private String theme = "light";

    @Column(nullable = false, length = 64)
    @Builder.Default
    private String timezone = "Asia/Shanghai";

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
