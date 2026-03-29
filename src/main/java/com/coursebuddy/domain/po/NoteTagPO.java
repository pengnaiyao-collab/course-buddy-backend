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
@TableName("note_tags")
public class NoteTagPO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private String color;
    @Builder.Default
    private Integer useCount = 0;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
