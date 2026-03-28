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
public class WebImportDTO {

    @NotBlank(message = "URL不能为空")
    @Size(max = 2048, message = "URL长度不能超过2048个字符")
    private String url;

    @Size(max = 64, message = "分类长度不能超过64个字符")
    private String category;

    @Size(max = 256, message = "标签长度不能超过256个字符")
    private String tags;

    /** Whether to automatically create a KnowledgeItem from the imported page */
    private boolean createKnowledgeItem = true;
}
