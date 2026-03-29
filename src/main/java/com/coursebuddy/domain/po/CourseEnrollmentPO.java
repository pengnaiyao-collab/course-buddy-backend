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
@TableName("course_enrollments")
public class CourseEnrollmentPO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private Long userId;
    @Builder.Default
    private String status = "ACTIVE";
    private LocalDateTime enrolledAt;
    private LocalDateTime droppedAt;
    private LocalDateTime completedAt;
}
