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
public class NoteTagDTO {

    @NotBlank(message = "Tag name is required")
    @Size(max = 64, message = "Tag name must not exceed 64 characters")
    private String name;

    @Size(max = 16)
    private String color;
}
