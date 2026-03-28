package com.coursebuddy.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 笔记导出请求数据传输对象。
 *
 * <p>调用方通过 {@code format} 字段指定导出格式：</p>
 * <ul>
 *   <li>{@code PDF}      – 生成 PDF 文档</li>
 *   <li>{@code MARKDOWN} – 生成 Markdown 文本文件</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteExportDTO {

    /**
     * 导出格式，仅接受 {@code PDF} 或 {@code MARKDOWN}（大小写不敏感）。
     */
    @NotBlank(message = "Export format is required")
    @Pattern(regexp = "(?i)PDF|MARKDOWN", message = "Format must be PDF or MARKDOWN")
    private String format;
}
