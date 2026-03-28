package com.coursebuddy.service;

import com.coursebuddy.domain.dto.NoteTagDTO;
import com.coursebuddy.domain.vo.NoteTagVO;

import java.util.List;

public interface INoteTagService {
    NoteTagVO create(NoteTagDTO dto);
    List<NoteTagVO> listMy(String keyword);
    NoteTagVO update(Long id, NoteTagDTO dto);
    void delete(Long id);
}
