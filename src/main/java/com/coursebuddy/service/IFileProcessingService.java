package com.coursebuddy.service;

import java.io.InputStream;

public interface IFileProcessingService {

    /**
     * Extract plain text from a file.
     *
     * @param inputStream file content
     * @param contentType MIME type of the file
     * @return extracted text, or empty string if unsupported format
     */
    String extractText(InputStream inputStream, String contentType);
}
