package com.coursebuddy.service;

import com.coursebuddy.domain.dto.KnowledgeAnalyzeDTO;
import com.coursebuddy.domain.dto.KnowledgeItemDTO;
import com.coursebuddy.domain.dto.KnowledgeResourceDTO;
import com.coursebuddy.domain.vo.KnowledgeAnalyzeResultVO;
import com.coursebuddy.domain.vo.KnowledgeGraphVO;
import com.coursebuddy.domain.vo.KnowledgeItemVO;
import com.coursebuddy.domain.vo.KnowledgeResourceVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IKnowledgeBaseService {
    KnowledgeItemVO create(KnowledgeItemDTO dto);
    KnowledgeItemVO createForCourse(Long courseId, KnowledgeItemDTO dto);
    Page<KnowledgeItemVO> listByCourse(Long courseId, Pageable pageable);
    KnowledgeItemVO getById(Long id);
    KnowledgeItemVO update(Long id, KnowledgeItemDTO dto);
    void delete(Long id);
    Page<KnowledgeItemVO> search(Long courseId, String keyword, Pageable pageable);
    Page<KnowledgeItemVO> searchAdvanced(Long courseId, String keyword, List<String> tags, String tagMode, Pageable pageable);
    KnowledgeAnalyzeResultVO autoAnalyze(Long courseId, KnowledgeAnalyzeDTO dto);
    KnowledgeGraphVO buildGraph(Long courseId);
    String generateMindMap(Long courseId, Long itemId);
    KnowledgeResourceVO addResource(Long courseId, Long itemId, KnowledgeResourceDTO dto);
    List<KnowledgeResourceVO> listResources(Long courseId, Long itemId);
    void deleteResource(Long courseId, Long itemId, Long resourceId);
}
