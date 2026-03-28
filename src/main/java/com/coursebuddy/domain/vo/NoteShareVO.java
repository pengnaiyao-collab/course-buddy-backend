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
public class NoteShareVO {
    private Long id;
    private Long noteId;
    private String noteTitle;
    private Long ownerId;
    private String shareToken;
    private String shareUrl;
    private String permission;
    private LocalDateTime expiresAt;
    private Integer accessCount;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
