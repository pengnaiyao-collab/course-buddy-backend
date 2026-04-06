package com.coursebuddy.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 课时持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("lessons")
public class LessonPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private String title;
    private String description;
    private String content;
    @Builder.Default
    private Integer lessonOrder = 1;

    @Builder.Default
    private Integer duration = 0;
    private String videoUrl;
    private String resourceUrls;
    @Builder.Default
    private Boolean isPublished = false;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private LocalDateTime deletedAt;
}
