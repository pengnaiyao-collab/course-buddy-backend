package com.coursebuddy.service.impl;

import com.coursebuddy.auth.User;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.domain.dto.NoteCategoryDTO;
import com.coursebuddy.domain.po.NoteCategoryPO;
import com.coursebuddy.domain.vo.NoteCategoryVO;
import com.coursebuddy.mapper.NoteCategoryMapper;
import com.coursebuddy.repository.NoteCategoryRepository;
import com.coursebuddy.service.INoteCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteCategoryServiceImpl implements INoteCategoryService {

    private final NoteCategoryRepository noteCategoryRepository;
    private final NoteCategoryMapper noteCategoryMapper;

    @Override
    @Transactional
    public NoteCategoryVO create(NoteCategoryDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (noteCategoryRepository.existsByUserIdAndName(currentUser.getId(), dto.getName())) {
            throw new BusinessException(409, "Category with this name already exists");
        }
        NoteCategoryPO po = noteCategoryMapper.dtoToPo(dto);
        po.setUserId(currentUser.getId());
        return noteCategoryMapper.poToVo(noteCategoryRepository.save(po));
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
        return noteCategoryMapper.poToVo(noteCategoryRepository.save(po));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        NoteCategoryPO po = noteCategoryRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new BusinessException(404, "Category not found"));
        noteCategoryRepository.delete(po);
    }
}
