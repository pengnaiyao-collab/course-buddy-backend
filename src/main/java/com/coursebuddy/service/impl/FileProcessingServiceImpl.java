package com.coursebuddy.service.impl;

import com.coursebuddy.service.IFileProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FileProcessingServiceImpl implements IFileProcessingService {

    @Override
    public String extractText(InputStream inputStream, String contentType) {
        if (contentType == null) {
            return "";
        }
        try {
            return switch (contentType.toLowerCase()) {
                case "application/pdf" -> extractFromPdf(inputStream);
                case "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                     "application/msword" -> extractFromWord(inputStream);
                case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                     "application/vnd.ms-excel" -> extractFromExcel(inputStream);
                default -> {
                    log.debug("Unsupported content type for text extraction: {}", contentType);
                    yield "";
                }
            };
        } catch (Exception e) {
            log.error("Failed to extract text from file with contentType {}: {}",
                    contentType, e.getMessage());
            return "";
        }
    }

    private String extractFromPdf(InputStream inputStream) throws Exception {
        try (PDDocument doc = Loader.loadPDF(
                org.apache.pdfbox.io.RandomAccessReadBuffer.createBufferFromStream(inputStream))) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }

    private String extractFromWord(InputStream inputStream) throws Exception {
        try (XWPFDocument doc = new XWPFDocument(inputStream)) {
            List<XWPFParagraph> paragraphs = doc.getParagraphs();
            return paragraphs.stream()
                    .map(XWPFParagraph::getText)
                    .collect(Collectors.joining("\n"));
        }
    }

    private String extractFromExcel(InputStream inputStream) throws Exception {
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        sb.append(cell.toString()).append("\t");
                    }
                    sb.append("\n");
                }
            }
            return sb.toString();
        }
    }
}
