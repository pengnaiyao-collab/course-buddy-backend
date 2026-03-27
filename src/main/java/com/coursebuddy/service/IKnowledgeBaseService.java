package com.coursebuddy.service;

import com.coursebuddy.domain.dto.KnowledgeItemDTO;
import com.coursebuddy.domain.vo.KnowledgeItemVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IKnowledgeBaseService {
    KnowledgeItemVO create(KnowledgeItemDTO dto);
    KnowledgeItemVO createForCourse(Long courseId, KnowledgeItemDTO dto);
    Page<KnowledgeItemVO> listByCourse(Long courseId, Pageable pageable);
    KnowledgeItemVO getById(Long id);
    KnowledgeItemVO update(Long id, KnowledgeItemDTO dto);
    void delete(Long id);
    Page<KnowledgeItemVO> search(Long courseId, String keyword, Pageable pageable);
}
