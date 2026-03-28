package com.coursebuddy.service;

/**
 * 笔记导出服务接口。
 *
 * <p>支持将笔记内容导出为 PDF 或 Markdown 格式。</p>
 */
public interface INoteExportService {

    /**
     * 将指定笔记导出为 PDF 字节数组。
     *
     * @param noteId 笔记 ID
     * @return PDF 文件的字节内容
     */
    byte[] exportToPdf(Long noteId);

    /**
     * 将指定笔记导出为 Markdown 字节数组（UTF-8 编码）。
     *
     * @param noteId 笔记 ID
     * @return Markdown 文件的字节内容
     */
    byte[] exportToMarkdown(Long noteId);

    /**
     * 按照指定格式导出笔记。
     *
     * @param noteId 笔记 ID
     * @param format 导出格式，{@code PDF} 或 {@code MARKDOWN}（大小写不敏感）
     * @return 导出文件的字节内容
     */
    byte[] export(Long noteId, String format);
}
