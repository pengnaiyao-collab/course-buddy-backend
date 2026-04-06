package com.coursebuddy.service;

import com.coursebuddy.domain.dto.NoteDTO;
import com.coursebuddy.domain.vo.NoteVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 笔记服务
 */
public interface INoteService {
    NoteVO create(NoteDTO dto);
    Page<NoteVO> listMyNotes(Long courseId, Pageable pageable);
    NoteVO getById(Long id);
    NoteVO update(Long id, NoteDTO dto);
    void delete(Long id);
}
