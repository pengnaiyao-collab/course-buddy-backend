package com.coursebuddy.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 笔记分类传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteCategoryDTO {

    @NotBlank(message = "Category name is required")
    @Size(max = 64, message = "Category name must not exceed 64 characters")
    private String name;

    /** 分类描述（可选）。 */
    private String description;

    @Size(max = 16)
    private String color;

    private Integer sortOrder;
}
