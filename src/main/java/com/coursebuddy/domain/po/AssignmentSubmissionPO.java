package com.coursebuddy.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("assignment_submissions")
public class AssignmentSubmissionPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long assignmentId;
    private Long studentId;
    private String submissionUrl;
    private LocalDateTime submittedAt;

    private Integer score;
    private String feedback;
    private LocalDateTime gradedAt;
    private Long gradedBy;
    @Builder.Default
    private String status = "SUBMITTED";
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
