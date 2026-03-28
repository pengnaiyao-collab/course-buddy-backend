package com.coursebuddy.service;

import com.coursebuddy.domain.dto.KnowledgeAssociationDTO;
import com.coursebuddy.domain.vo.KnowledgeAssociationVO;

import java.util.List;

/**
 * Service for managing associations between knowledge items.
 */
public interface IKnowledgeAssociationService {

    /**
     * Create an association from sourceId to targetId.
     */
    KnowledgeAssociationVO createAssociation(Long sourceId, KnowledgeAssociationDTO dto);

    /**
     * List all associations where the given item is the source.
     */
    List<KnowledgeAssociationVO> listBySource(Long sourceId);

    /**
     * List all associations where the given item is the target.
     */
    List<KnowledgeAssociationVO> listByTarget(Long targetId);

    /**
     * Delete a specific association.
     */
    void deleteAssociation(Long associationId);

    /**
     * Delete association between two items.
     */
    void deleteAssociation(Long sourceId, Long targetId);
}
