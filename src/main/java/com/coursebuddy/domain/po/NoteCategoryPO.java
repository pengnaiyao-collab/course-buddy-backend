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
@TableName("note_categories")
public class NoteCategoryPO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;

    /** 分类描述（可选）。 */
    private String description;
    private String color;
    @Builder.Default
    private Integer sortOrder = 0;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
