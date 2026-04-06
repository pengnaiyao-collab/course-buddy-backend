package com.coursebuddy.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("attendances")
public class AttendancePO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private Long studentId;
    private LocalDate sessionDate;
    @Builder.Default
    private String status = "PRESENT";
    private String remarks;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
