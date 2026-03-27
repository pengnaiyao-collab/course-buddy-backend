package com.coursebuddy.notes;

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
public class NoteRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 256, message = "Title must not exceed 256 characters")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    private Long courseId;

    @Size(max = 64, message = "Category must not exceed 64 characters")
    private String category;

    @Size(max = 256, message = "Tags must not exceed 256 characters")
    private String tags;
}
