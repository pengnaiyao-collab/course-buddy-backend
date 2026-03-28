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
@Table(name = "team_invitations")
public class TeamInvitationPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(name = "invited_user_id", nullable = false)
    private Long invitedUserId;

    @Column(name = "invited_by", nullable = false)
    private Long invitedBy;

    @Column(name = "role", nullable = false, length = 16)
    @Builder.Default
    private String role = "MEMBER";

    @Column(nullable = false, length = 16)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (expiredAt == null) {
            expiredAt = LocalDateTime.now().plusDays(7);
        }
    }
}
