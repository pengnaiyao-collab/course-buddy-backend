package com.coursebuddy.service.impl;

import com.coursebuddy.auth.User;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.domain.dto.KnowledgeAssociationDTO;
import com.coursebuddy.domain.po.KnowledgeAssociationPO;
import com.coursebuddy.domain.po.KnowledgeItemPO;
import com.coursebuddy.domain.vo.KnowledgeAssociationVO;
import com.coursebuddy.repository.KnowledgeAssociationRepository;
import com.coursebuddy.repository.KnowledgeItemRepository;
import com.coursebuddy.service.IKnowledgeAssociationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class KnowledgeAssociationServiceImpl implements IKnowledgeAssociationService {

    private final KnowledgeAssociationRepository associationRepository;
    private final KnowledgeItemRepository knowledgeItemRepository;

    @Override
    @Transactional
    public KnowledgeAssociationVO createAssociation(Long sourceId, KnowledgeAssociationDTO dto) {
        if (sourceId.equals(dto.getTargetId())) {
            throw new BusinessException("不能关联知识点到自身");
        }
        KnowledgeItemPO source = knowledgeItemRepository.findById(sourceId)
                .orElseThrow(() -> new BusinessException(404, "源知识点不存在: " + sourceId));
        KnowledgeItemPO target = knowledgeItemRepository.findById(dto.getTargetId())
                .orElseThrow(() -> new BusinessException(404, "目标知识点不存在: " + dto.getTargetId()));

        if (associationRepository.existsBySourceIdAndTargetId(sourceId, dto.getTargetId())) {
            throw new BusinessException("关联关系已存在");
        }

        Long userId = null;
        try {
            User user = SecurityUtils.getCurrentUser();
            userId = user.getId();
        } catch (Exception ignored) {}

        String relationType = dto.getRelationType() != null && !dto.getRelationType().isBlank()
                ? dto.getRelationType() : "RELATED";

        KnowledgeAssociationPO po = KnowledgeAssociationPO.builder()
                .sourceId(sourceId)
                .targetId(dto.getTargetId())
                .relationType(relationType)
                .description(dto.getDescription())
                .createdBy(userId)
                .build();

        KnowledgeAssociationPO saved = associationRepository.save(po);
        log.info("Created association {} -> {} ({})", sourceId, dto.getTargetId(), relationType);

        return toVO(saved, target.getTitle());
    }

    @Override
    @Transactional(readOnly = true)
    public List<KnowledgeAssociationVO> listBySource(Long sourceId) {
        return associationRepository.findBySourceId(sourceId).stream()
                .map(a -> {
                    String targetTitle = knowledgeItemRepository.findById(a.getTargetId())
                            .map(KnowledgeItemPO::getTitle).orElse(null);
                    return toVO(a, targetTitle);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<KnowledgeAssociationVO> listByTarget(Long targetId) {
        return associationRepository.findByTargetId(targetId).stream()
                .map(a -> toVO(a, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAssociation(Long associationId) {
        if (!associationRepository.existsById(associationId)) {
            throw new BusinessException(404, "关联关系不存在");
        }
        associationRepository.deleteById(associationId);
    }

    @Override
    @Transactional
    public void deleteAssociation(Long sourceId, Long targetId) {
        if (!associationRepository.existsBySourceIdAndTargetId(sourceId, targetId)) {
            throw new BusinessException(404, "关联关系不存在");
        }
        associationRepository.deleteBySourceIdAndTargetId(sourceId, targetId);
    }

    private KnowledgeAssociationVO toVO(KnowledgeAssociationPO po, String targetTitle) {
        return KnowledgeAssociationVO.builder()
                .id(po.getId())
                .sourceId(po.getSourceId())
                .targetId(po.getTargetId())
                .relationType(po.getRelationType())
                .description(po.getDescription())
                .createdBy(po.getCreatedBy())
                .createdAt(po.getCreatedAt())
                .targetTitle(targetTitle)
                .build();
    }
}
