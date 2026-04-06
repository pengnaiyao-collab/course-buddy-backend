package com.coursebuddy.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 笔记版本持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("note_versions")
public class NoteVersionPO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long noteId;
    private Integer versionNo;
    private String title;
    private String content;
    private Long changedBy;
    private String changeDesc;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
