package com.coursebuddy.controller;

import com.coursebuddy.domain.dto.NoteExportDTO;
import com.coursebuddy.service.INoteExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 笔记导出 REST 控制器。
 *
 * <p>提供将笔记内容导出为 PDF 或 Markdown 格式文件的接口。</p>
 */
@Slf4j
@Tag(name = "Note Export", description = "Note export endpoints (PDF / Markdown)")
@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteExportController {

    private final INoteExportService exportService;

    /**
     * 将指定笔记导出为文件。
     *
     * <p>根据请求体中的 {@code format} 字段决定导出格式：</p>
     * <ul>
     *   <li>{@code PDF}      – 返回 {@code application/pdf} 附件</li>
     *   <li>{@code MARKDOWN} – 返回 {@code text/markdown} 附件</li>
     * </ul>
     *
     * @param noteId 笔记 ID（路径参数）
     * @param dto    导出请求（包含 format 字段）
     * @return 二进制文件内容，携带正确的 Content-Type 和 Content-Disposition 头
     */
    @Operation(
            summary = "Export a note to PDF or Markdown",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{noteId}/export")
    public ResponseEntity<byte[]> export(
            @Parameter(description = "Note ID") @PathVariable Long noteId,
            @Valid @RequestBody NoteExportDTO dto) {

        log.info("Export request: noteId={}, format={}", noteId, dto.getFormat());

        byte[] content = exportService.export(noteId, dto.getFormat());

        boolean isPdf = "PDF".equalsIgnoreCase(dto.getFormat());
        MediaType mediaType  = isPdf ? MediaType.APPLICATION_PDF
                                     : MediaType.parseMediaType("text/markdown;charset=UTF-8");
        String    filename   = "note-" + noteId + (isPdf ? ".pdf" : ".md");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDisposition(
                ContentDisposition.attachment().filename(filename).build());
        headers.setContentLength(content.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(content);
    }
}
