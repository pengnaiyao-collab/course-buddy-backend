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
public class UserActivityVO {
    private Long userId;
    private String username;
    private Long projectId;
    private long totalActions;
    private long taskCreated;
    private long taskCompleted;
    private long commentsAdded;
    private LocalDateTime lastActiveAt;
    private List<CollaborationLogVO> recentLogs;
}
