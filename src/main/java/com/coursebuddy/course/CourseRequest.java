package com.coursebuddy.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 128, message = "Title must not exceed 128 characters")
    private String title;

    private String description;

    @PositiveOrZero(message = "Price must be non-negative")
    private BigDecimal price;

    @Size(max = 64, message = "Category must not exceed 64 characters")
    private String category;

    private CourseStatus status;

    private Integer maxStudents;

    private String coverImageUrl;
}
