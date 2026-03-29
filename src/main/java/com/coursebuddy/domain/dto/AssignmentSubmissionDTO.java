package com.coursebuddy.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssignmentSubmissionDTO {
    @NotBlank
    private String submissionUrl;
}
