package com.coursebuddy.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamInvitationVO {
    private Long id;
    private Long teamId;
    private String teamName;
    private Long invitedUserId;
    private String invitedUsername;
    private Long invitedBy;
    private String inviterName;
    private String role;
    private String status;
    private LocalDateTime expiredAt;
    private LocalDateTime createdAt;
}
