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
public class CollaborationLogVO {
    private Long id;
    private Long projectId;
    private Long userId;
    private String username;
    private String actionType;
    private String entityType;
    private Long entityId;
    private String changeData;
    private LocalDateTime createdAt;
}
