package com.coursebuddy.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {

    @NotBlank(message = "Question content is required")
    private String content;

    private Long courseId;

    @Size(max = 64, message = "Subject must not exceed 64 characters")
    private String subject;
}
