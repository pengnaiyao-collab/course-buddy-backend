package com.coursebuddy.course;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("courses")
public class Course {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    private Long teacherId;

    private BigDecimal price;

    private String category;

    @Builder.Default
    private CourseStatus status = CourseStatus.DRAFT;

    private Integer maxStudents;

    private String coverImageUrl;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
