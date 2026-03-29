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
public class KnowledgeItemDTO {

    @NotBlank(message = "Title is required")
    @Size(max = 256, message = "Title must not exceed 256 characters")
    private String title;

    private String description;

    private String content;

    @Size(max = 512, message = "File URL must not exceed 512 characters")
    private String fileUrl;

    @Size(max = 64, message = "File type must not exceed 64 characters")
    private String fileType;

    @Size(max = 64, message = "Category must not exceed 64 characters")
    private String category;

    @Size(max = 256, message = "Tags must not exceed 256 characters")
    private String tags;

    @Size(max = 64, message = "Source type must not exceed 64 characters")
    private String sourceType;

    @Size(max = 32, message = "Status must not exceed 32 characters")
    private String status;
}
