package com.coursebuddy.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourseAdminVoteDTO {
    @NotNull
    private Long courseId;
    @NotNull
    private Long candidateUserId;
}
