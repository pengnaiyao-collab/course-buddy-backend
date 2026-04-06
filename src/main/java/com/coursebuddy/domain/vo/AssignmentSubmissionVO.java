package com.coursebuddy.domain.vo;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 作业提交视图对象
 */
@Data
@Builder
public class AssignmentSubmissionVO {
    private Long id;
    private Long assignmentId;
    private Long studentId;
    private String studentName;
    private String submissionUrl;
    private LocalDateTime submittedAt;
    private Double score;
    private String feedback;
    private LocalDateTime gradedAt;
    private Long gradedBy;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
