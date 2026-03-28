package com.coursebuddy.service;

import com.coursebuddy.domain.dto.WebImportDTO;
import com.coursebuddy.domain.vo.WebImportVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service for importing web pages into the knowledge base.
 */
public interface IWebImportService {

    /**
     * Import a web page for a course.
     *
     * @param courseId course identifier
     * @param dto      import request with URL and optional metadata
     * @return import result with extracted content
     */
    WebImportVO importWebPage(Long courseId, WebImportDTO dto);

    /**
     * List all web imports for a course.
     */
    Page<WebImportVO> listByCourse(Long courseId, Pageable pageable);

    /**
     * Get a specific web import record.
     */
    WebImportVO getById(Long id);

    /**
     * Delete a web import record.
     */
    void delete(Long id);
}
