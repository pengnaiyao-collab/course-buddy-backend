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
@TableName("note_shares")
public class NoteSharePO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long noteId;
    private Long ownerId;
    private String shareToken;
    @Builder.Default
    private String permission = "READ";
    private LocalDateTime expiresAt;
    @Builder.Default
    private Integer accessCount = 0;
    @Builder.Default
    private Boolean isActive = true;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
