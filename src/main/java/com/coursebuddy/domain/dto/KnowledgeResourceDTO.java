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
public class KnowledgeResourceDTO {

    @NotBlank(message = "Resource type is required")
    @Size(max = 32, message = "Resource type must not exceed 32 characters")
    private String resourceType;

    @NotBlank(message = "Title is required")
    @Size(max = 256, message = "Title must not exceed 256 characters")
    private String title;

    @NotBlank(message = "URL is required")
    @Size(max = 512, message = "URL must not exceed 512 characters")
    private String url;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}
