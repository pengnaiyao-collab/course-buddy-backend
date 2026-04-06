package com.coursebuddy.service.impl;

import com.coursebuddy.domain.auth.User;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.domain.dto.NoteCategoryDTO;
import com.coursebuddy.domain.po.NoteCategoryPO;
import com.coursebuddy.domain.vo.NoteCategoryVO;
import com.coursebuddy.converter.NoteCategoryConverter;
import com.coursebuddy.mapper.NoteCategoryMapper;
import com.coursebuddy.service.INoteCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 笔记分类服务实现
 */
@Service
@RequiredArgsConstructor
public class NoteCategoryServiceImpl implements INoteCategoryService {

    private final NoteCategoryMapper noteCategoryRepository;
    private final NoteCategoryConverter noteCategoryMapper;

    @Override
    @Transactional
    public NoteCategoryVO create(NoteCategoryDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (noteCategoryRepository.existsByUserIdAndName(currentUser.getId(), dto.getName())) {
            throw new BusinessException(409, "Category with this name already exists");
        }
        NoteCategoryPO po = noteCategoryMapper.dtoToPo(dto);
        po.setUserId(currentUser.getId());
        noteCategoryRepository.insert(po);
        return noteCategoryMapper.poToVo(po);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteCategoryVO> listMy() {
        User currentUser = SecurityUtils.getCurrentUser();
        return noteCategoryMapper.poListToVoList(
                noteCategoryRepository.findByUserIdOrderBySortOrderAsc(currentUser.getId()));
    }

    @Override
    @Transactional
    public NoteCategoryVO update(Long id, NoteCategoryDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        NoteCategoryPO po = noteCategoryRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new BusinessException(404, "Category not found"));
        if (dto.getName() != null) po.setName(dto.getName());
        if (dto.getColor() != null) po.setColor(dto.getColor());
        if (dto.getSortOrder() != null) po.setSortOrder(dto.getSortOrder());
        noteCategoryRepository.updateById(po);
        return noteCategoryMapper.poToVo(po);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        NoteCategoryPO po = noteCategoryRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new BusinessException(404, "Category not found"));
        noteCategoryRepository.deleteById(po.getId());
    }
}
