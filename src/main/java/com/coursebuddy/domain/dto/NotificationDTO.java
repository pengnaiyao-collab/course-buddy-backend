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
public class NotificationDTO {

    @NotBlank(message = "Title is required")
    @Size(max = 256)
    private String title;

    private String content;

    @Size(max = 32)
    private String type;

    private Long relatedId;

    private String relatedType;
}
