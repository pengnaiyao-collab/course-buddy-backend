package com.coursebuddy.service.impl;

import com.coursebuddy.service.IOcrService;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;

@Service
@Slf4j
public class OcrServiceImpl implements IOcrService {

    @Value("${ocr.tessdata-path:#{null}}")
    private String tessdataPath;

    @Value("${ocr.enabled:true}")
    private boolean ocrEnabled;

    @Override
    public String extractTextFromImage(InputStream inputStream, String filename, String language) {
        if (!ocrEnabled) {
            log.debug("OCR is disabled by configuration");
            return "";
        }
        try {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                log.warn("Could not read image from stream for file: {}", filename);
                return "";
            }
            Tesseract tesseract = new Tesseract();
            if (tessdataPath != null && !tessdataPath.isBlank()) {
                tesseract.setDatapath(tessdataPath);
            }
            tesseract.setLanguage(language != null ? language : "chi_sim+eng");
            String result = tesseract.doOCR(image);
            log.info("OCR completed for file: {}, extracted {} chars", filename,
                    result != null ? result.length() : 0);
            return result != null ? result.trim() : "";
        } catch (TesseractException e) {
            log.warn("Tesseract OCR failed for file {}: {}", filename, e.getMessage());
            return "";
        } catch (Exception e) {
            log.error("OCR extraction failed for file {}: {}", filename, e.getMessage(), e);
            return "";
        }
    }

    @Override
    public boolean isAvailable() {
        if (!ocrEnabled) return false;
        try {
            Tesseract tesseract = new Tesseract();
            if (tessdataPath != null && !tessdataPath.isBlank()) {
                tesseract.setDatapath(tessdataPath);
            }
            BufferedImage blankImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
            tesseract.doOCR(blankImage);
            return true;
        } catch (UnsatisfiedLinkError | TesseractException e) {
            log.debug("Tesseract OCR not available: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.debug("Tesseract OCR availability check failed: {}", e.getMessage());
            return false;
        }
    }
}
