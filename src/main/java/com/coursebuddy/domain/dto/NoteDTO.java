package com.coursebuddy.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteDTO {

    @NotBlank(message = "Title is required")
    @Size(max = 256, message = "Title must not exceed 256 characters")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    private Long courseId;

    /** 所属分类 ID（可选）。 */
    private Long categoryId;

    /** 笔记摘要/描述（可选）。 */
    private String description;

    /**
     * 笔记状态：DRAFT、PUBLISHED、ARCHIVED。默认 DRAFT。
     */
    @Pattern(regexp = "DRAFT|PUBLISHED|ARCHIVED", message = "Status must be DRAFT, PUBLISHED or ARCHIVED")
    private String status;

    @Size(max = 64, message = "Category must not exceed 64 characters")
    private String category;

    @Size(max = 256, message = "Tags must not exceed 256 characters")
    private String tags;

    /** 是否公开分享（默认 false）。 */
    private Boolean isPublic;

    /**
     * 关联标签 ID 列表（可选，用于多对多关联）。
     */
    private List<Long> tagIds;
}

