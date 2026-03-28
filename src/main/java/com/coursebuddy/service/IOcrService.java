package com.coursebuddy.service;

import java.io.InputStream;

/**
 * OCR service for extracting text from images and scanned documents.
 */
public interface IOcrService {

    /**
     * Extract text from an image input stream.
     *
     * @param inputStream image content
     * @param filename    original filename (used to detect format)
     * @param language    Tesseract language code, e.g. "chi_sim+eng"
     * @return extracted text, or empty string on failure
     */
    String extractTextFromImage(InputStream inputStream, String filename, String language);

    /**
     * Extract text using the default language (chi_sim+eng).
     */
    default String extractTextFromImage(InputStream inputStream, String filename) {
        return extractTextFromImage(inputStream, filename, "chi_sim+eng");
    }

    /**
     * Returns whether OCR is available in this environment (i.e. Tesseract is installed).
     */
    boolean isAvailable();
}
