package com.coursebuddy.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("team_invitations")
public class TeamInvitationPO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long teamId;
    private Long invitedUserId;
    private Long invitedBy;
    @Builder.Default
    private String role = "MEMBER";
    @Builder.Default
    private String status = "PENDING";
    private LocalDateTime expiredAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
