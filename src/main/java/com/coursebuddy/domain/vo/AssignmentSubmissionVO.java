package com.coursebuddy.domain.vo;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class AssignmentSubmissionVO {
    private Long id;
    private Long assignmentId;
    private Long studentId;
    private String submissionUrl;
    private LocalDateTime submittedAt;
    private Integer score;
    private String feedback;
    private LocalDateTime gradedAt;
    private Long gradedBy;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
