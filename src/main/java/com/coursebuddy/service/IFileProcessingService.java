package com.coursebuddy.service;

import java.io.InputStream;

/**
 * 文件服务
 */
public interface IFileProcessingService {

    /**
     * 从文件中提取纯文本。
     *
     * @param inputStream 文件内容
     * @param contentType 文件的 MIME 类型
     * @return 提取到的文本；若格式不支持则返回空字符串
     */
    String extractText(InputStream inputStream, String contentType);
}
