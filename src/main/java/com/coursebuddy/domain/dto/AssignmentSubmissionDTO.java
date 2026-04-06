package com.coursebuddy.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 作业提交传输对象
 */
@Data
public class AssignmentSubmissionDTO {
    @NotBlank
    private String submissionUrl;
}
