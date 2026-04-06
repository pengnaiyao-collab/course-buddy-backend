package com.coursebuddy.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 笔记传输对象
 */
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

    @Size(max = 64, message = "Category must not exceed 64 characters")
    private String category;

    /** 附件链接列表（可选）。 */
    private List<String> attachments;

    /** 是否公开分享（默认 false）。 */
    private Boolean isPublic;
}

