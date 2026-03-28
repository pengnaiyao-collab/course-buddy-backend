package com.coursebuddy.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamVO {
    private Long id;
    private String name;
    private String description;
    private String avatarUrl;
    private Long ownerId;
    private String ownerName;
    private Long courseId;
    private Long projectId;
    private Integer memberCount;
    private List<TeamMemberVO> members;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
