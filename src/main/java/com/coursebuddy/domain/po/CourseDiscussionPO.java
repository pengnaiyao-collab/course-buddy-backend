package com.coursebuddy.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 课程讨论持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("course_discussions")
public class CourseDiscussionPO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private Long parentId;
    private Long authorId;
    private String title;
    private String content;
    @Builder.Default
    private Integer likeCount = 0;
    @Builder.Default
    private Boolean isPinned = false;
    @Builder.Default
    private Boolean isDeleted = false;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
