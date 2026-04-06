package com.coursebuddy.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 笔记版本视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteVersionVO {
    private Long id;
    private Long noteId;
    private Integer versionNo;
    private String title;
    private String content;
    private Long changedBy;
    private String changedByName;
    private String changeDesc;
    private LocalDateTime createdAt;
}
