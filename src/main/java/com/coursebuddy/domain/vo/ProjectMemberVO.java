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
public class ProjectMemberVO {
    private Long id;
    private Long projectId;
    private Long userId;
    private String username;
    private String email;
    private String role;
    private LocalDateTime joinedAt;
    private LocalDateTime createdAt;
}
