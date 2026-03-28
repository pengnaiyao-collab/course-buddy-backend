package com.coursebuddy.service;

import com.coursebuddy.domain.dto.NoteCategoryDTO;
import com.coursebuddy.domain.vo.NoteCategoryVO;

import java.util.List;

public interface INoteCategoryService {
    NoteCategoryVO create(NoteCategoryDTO dto);
    List<NoteCategoryVO> listMy();
    NoteCategoryVO update(Long id, NoteCategoryDTO dto);
    void delete(Long id);
}
