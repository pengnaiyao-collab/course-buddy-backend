package com.coursebuddy.service.impl;

import com.coursebuddy.auth.User;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.domain.dto.KnowledgeItemDTO;
import com.coursebuddy.domain.po.KnowledgeItemPO;
import com.coursebuddy.domain.vo.KnowledgeItemVO;
import com.coursebuddy.mapper.KnowledgeItemMapper;
import com.coursebuddy.repository.KnowledgeItemRepository;
import com.coursebuddy.service.IKnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KnowledgeBaseServiceImpl implements IKnowledgeBaseService {

    private final KnowledgeItemRepository repository;
    private final KnowledgeItemMapper mapper;

    @Override
    @Transactional
    public KnowledgeItemVO create(KnowledgeItemDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        KnowledgeItemPO po = mapper.dtoToPo(dto);
        po.setCreatedBy(currentUser.getId());
        return mapper.poToVo(repository.save(po));
    }

    @Override
    @Transactional
    public KnowledgeItemVO createForCourse(Long courseId, KnowledgeItemDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        KnowledgeItemPO po = mapper.dtoToPo(dto);
        po.setCourseId(courseId);
        po.setCreatedBy(currentUser.getId());
        return mapper.poToVo(repository.save(po));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KnowledgeItemVO> listByCourse(Long courseId, Pageable pageable) {
        return mapper.poPageToVoPage(repository.findByCourseId(courseId, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public KnowledgeItemVO getById(Long id) {
        KnowledgeItemPO po = repository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Knowledge item not found"));
        return mapper.poToVo(po);
    }

    @Override
    @Transactional
    public KnowledgeItemVO update(Long id, KnowledgeItemDTO dto) {
        KnowledgeItemPO existing = repository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Knowledge item not found"));
        existing.setTitle(dto.getTitle());
        existing.setDescription(dto.getDescription());
        existing.setFileUrl(dto.getFileUrl());
        existing.setFileType(dto.getFileType());
        existing.setCategory(dto.getCategory());
        existing.setTags(dto.getTags());
        return mapper.poToVo(repository.save(existing));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new BusinessException(404, "Knowledge item not found");
        }
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KnowledgeItemVO> search(Long courseId, String keyword, Pageable pageable) {
        return mapper.poPageToVoPage(
                repository.findByCourseIdAndTitleContainingIgnoreCase(courseId, keyword, pageable));
    }

}

