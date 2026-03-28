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
public class TaskCommentDTO {

    @NotBlank(message = "Comment content is required")
    private String content;

    @Size(max = 512, message = "Attachment URL must not exceed 512 characters")
    private String attachmentUrl;
}
