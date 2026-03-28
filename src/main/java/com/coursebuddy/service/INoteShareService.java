package com.coursebuddy.service;

import com.coursebuddy.domain.dto.NoteShareDTO;
import com.coursebuddy.domain.vo.NoteShareVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface INoteShareService {
    NoteShareVO createShare(Long noteId, NoteShareDTO dto);
    Page<NoteShareVO> listMyShares(Long noteId, Pageable pageable);
    NoteShareVO getByToken(String shareToken);
    NoteShareVO updateShare(Long shareId, NoteShareDTO dto);
    void deleteShare(Long shareId);
}
