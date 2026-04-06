package com.coursebuddy.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 课程持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("course_catalog")
public class CoursePO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private String name;
    private String description;
    private Long instructorId;
    private Long departmentId;
    @Builder.Default
    private Integer creditHours = 3;
    @Builder.Default
    private String level = "BEGINNER";
    @Builder.Default
    private Integer capacity = 30;
    @Builder.Default
    private Integer enrolledCount = 0;
    private String thumbnailUrl;
    private String syllabus;
    @Builder.Default
    private Integer maxGrade = 100;
    @Builder.Default
    private Integer passingGrade = 60;
    @Builder.Default
    private String status = "OPEN";
    private LocalDate startDate;
    private LocalDate endDate;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private LocalDateTime deletedAt;
}
