package com.coursebuddy.domain.vo;

import lombok.Data;

/**
 * 作业提交数量视图对象
 */
@Data
public class AssignmentSubmissionCountVO {
    private Long assignmentId;
    private Long submissionCount;
}
