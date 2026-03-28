package com.coursebuddy.service.impl;

import com.coursebuddy.auth.User;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.domain.po.NotePO;
import com.coursebuddy.repository.NoteRepository;
import com.coursebuddy.service.INoteExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 笔记导出服务实现。
 *
 * <p>使用 Apache PDFBox 3.x 生成 PDF；Markdown 导出则将笔记内容直接以 UTF-8 文本输出。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoteExportServiceImpl implements INoteExportService {

    /** A4 页面宽度（点）。 */
    private static final float PAGE_WIDTH  = PDRectangle.A4.getWidth();
    /** A4 页面高度（点）。 */
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    /** 页面边距（点）。 */
    private static final float MARGIN      = 50f;
    /** 正文字号。 */
    private static final float BODY_FONT_SIZE  = 11f;
    /** 标题字号。 */
    private static final float TITLE_FONT_SIZE = 16f;
    /** 行距。 */
    private static final float LEADING = 15f;
    /** 可用内容宽度。 */
    private static final float CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final NoteRepository noteRepository;

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public byte[] export(Long noteId, String format) {
        if ("PDF".equalsIgnoreCase(format)) {
            return exportToPdf(noteId);
        } else if ("MARKDOWN".equalsIgnoreCase(format)) {
            return exportToMarkdown(noteId);
        }
        throw new BusinessException(400, "Unsupported export format: " + format);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public byte[] exportToPdf(Long noteId) {
        NotePO note = loadNote(noteId);
        log.info("Exporting note {} to PDF for user {}", noteId, note.getUserId());

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font metaFont  = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);
            PDType1Font bodyFont  = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            // Build wrapped content lines
            List<String> lines = new ArrayList<>();
            for (String rawLine : note.getContent().split("\r?\n", -1)) {
                lines.addAll(wrapLine(rawLine, bodyFont, BODY_FONT_SIZE, CONTENT_WIDTH));
            }

            // Meta text (built before inner try block so it remains in scope)
            String meta = buildMeta(note);

            // --- First page: title block + content ---
            PDPage firstPage = new PDPage(PDRectangle.A4);
            document.addPage(firstPage);
            float y = PAGE_HEIGHT - MARGIN;

            int firstPageLineCount;
            try (PDPageContentStream cs = new PDPageContentStream(document, firstPage)) {
                // Title
                y = drawText(cs, note.getTitle(), titleFont, TITLE_FONT_SIZE, MARGIN, y);
                y -= 6f;

                // Meta line
                y = drawText(cs, meta, metaFont, 9f, MARGIN, y);
                y -= LEADING;

                // Separator
                cs.setLineWidth(0.5f);
                cs.moveTo(MARGIN, y);
                cs.lineTo(PAGE_WIDTH - MARGIN, y);
                cs.stroke();
                y -= LEADING;

                // Render lines that fit on the first page
                int count = 0;
                for (String line : lines) {
                    if (y < MARGIN + LEADING) break;
                    cs.beginText();
                    cs.setFont(bodyFont, BODY_FONT_SIZE);
                    cs.newLineAtOffset(MARGIN, y);
                    cs.showText(sanitizePdfText(line));
                    cs.endText();
                    y -= LEADING;
                    count++;
                }
                firstPageLineCount = count;
            }

            // Overflow: additional pages for remaining lines
            if (firstPageLineCount < lines.size()) {
                addOverflowPages(document, bodyFont, lines, firstPageLineCount);
            }

            document.save(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Failed to generate PDF for note {}: {}", noteId, e.getMessage(), e);
            throw new BusinessException(500, "Failed to generate PDF export");
        }
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public byte[] exportToMarkdown(Long noteId) {
        NotePO note = loadNote(noteId);
        log.info("Exporting note {} to Markdown for user {}", noteId, note.getUserId());

        String md = buildMarkdown(note);
        return md.getBytes(StandardCharsets.UTF_8);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * 加载笔记并校验当前用户的访问权限。
     *
     * @param noteId 笔记 ID
     * @return 笔记 PO 对象
     * @throws BusinessException 若笔记不存在或当前用户无权访问
     */
    private NotePO loadNote(Long noteId) {
        User currentUser = SecurityUtils.getCurrentUser();
        NotePO note = noteRepository.findById(noteId)
                .orElseThrow(() -> new BusinessException(404, "Note not found"));
        if (!note.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(403, "You are not authorized to export this note");
        }
        return note;
    }

    /**
     * 将笔记内容格式化为 Markdown 字符串。
     *
     * @param note 笔记 PO
     * @return Markdown 文本
     */
    private String buildMarkdown(NotePO note) {
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(note.getTitle()).append("\n\n");
        if (note.getCategory() != null && !note.getCategory().isBlank()) {
            sb.append("**Category:** ").append(note.getCategory()).append("\n\n");
        }
        if (note.getTags() != null && !note.getTags().isBlank()) {
            sb.append("**Tags:** ").append(note.getTags()).append("\n\n");
        }
        sb.append("**Created:** ").append(note.getCreatedAt().format(DATE_FMT)).append("  \n");
        sb.append("**Updated:** ").append(note.getUpdatedAt().format(DATE_FMT)).append("\n\n");
        sb.append("---\n\n");
        sb.append(note.getContent()).append("\n");
        return sb.toString();
    }

    /**
     * 构建 PDF 元数据行文本（创建时间、更新时间、分类、标签）。
     *
     * @param note 笔记 PO
     * @return 元数据字符串
     */
    private String buildMeta(NotePO note) {
        StringBuilder sb = new StringBuilder();
        sb.append("Created: ").append(note.getCreatedAt().format(DATE_FMT));
        sb.append("  |  Updated: ").append(note.getUpdatedAt().format(DATE_FMT));
        if (note.getCategory() != null && !note.getCategory().isBlank()) {
            sb.append("  |  Category: ").append(note.getCategory());
        }
        if (note.getTags() != null && !note.getTags().isBlank()) {
            sb.append("  |  Tags: ").append(note.getTags());
        }
        return sb.toString();
    }

    /**
     * 在 PDF 页面上绘制一行文本并返回下一行的 Y 坐标。
     */
    private float drawText(PDPageContentStream cs, String text, PDType1Font font,
                           float fontSize, float x, float y) throws IOException {
        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(x, y);
        cs.showText(sanitizePdfText(text));
        cs.endText();
        return y - fontSize - 4f;
    }

    /**
     * 将长文本行按可用宽度自动折行，返回折行后的行列表。
     */
    private List<String> wrapLine(String line, PDType1Font font, float fontSize,
                                  float maxWidth) throws IOException {
        List<String> result = new ArrayList<>();
        if (line.isBlank()) {
            result.add("");
            return result;
        }
        String[] words = line.split(" ");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            String candidate = current.isEmpty() ? word : current + " " + word;
            float width = font.getStringWidth(sanitizePdfText(candidate)) / 1000f * fontSize;
            if (width > maxWidth && !current.isEmpty()) {
                result.add(current.toString());
                current = new StringBuilder(word);
            } else {
                current = new StringBuilder(candidate);
            }
        }
        if (!current.isEmpty()) {
            result.add(current.toString());
        }
        return result;
    }

    /**
     * 将超出第一页的行分配到后续页面中。
     *
     * @param document   PDDocument 对象
     * @param bodyFont   正文字体
     * @param lines      全部内容行
     * @param startIndex 从第几行开始渲染（第一页已渲染的行之后）
     */
    private void addOverflowPages(PDDocument document, PDType1Font bodyFont,
                                   List<String> lines, int startIndex) throws IOException {
        PDPageContentStream cs = null;
        float y = 0;
        try {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            cs = new PDPageContentStream(document, page);
            y = PAGE_HEIGHT - MARGIN;

            for (int i = startIndex; i < lines.size(); i++) {
                if (y < MARGIN + LEADING) {
                    cs.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    cs = new PDPageContentStream(document, page);
                    y = PAGE_HEIGHT - MARGIN;
                }
                cs.beginText();
                cs.setFont(bodyFont, BODY_FONT_SIZE);
                cs.newLineAtOffset(MARGIN, y);
                cs.showText(sanitizePdfText(lines.get(i)));
                cs.endText();
                y -= LEADING;
            }
        } finally {
            if (cs != null) {
                cs.close();
            }
        }
    }

    /**
     * 移除 PDFBox {@link PDType1Font} 无法编码的非 Latin-1 字符，以防止 IllegalArgumentException。
     * 若字符超出 Latin-1 范围则替换为 {@code ?}。
     *
     * @param text 原始字符串
     * @return 安全的 Latin-1 字符串
     */
    private String sanitizePdfText(String text) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            if (c < 256) {
                sb.append(c);
            } else {
                sb.append('?');
            }
        }
        return sb.toString();
    }
}
