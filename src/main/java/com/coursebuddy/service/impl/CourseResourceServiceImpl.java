package com.coursebuddy.service.impl;

import com.coursebuddy.auth.Role;
import com.coursebuddy.auth.User;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.domain.dto.CourseResourceDTO;
import com.coursebuddy.domain.po.CourseResourcePO;
import com.coursebuddy.domain.vo.CourseResourceVO;
import com.coursebuddy.mapper.CourseResourceMapper;
import com.coursebuddy.repository.CourseResourceRepository;
import com.coursebuddy.service.ICourseResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseResourceServiceImpl implements ICourseResourceService {

    private final CourseResourceRepository resourceRepository;
    private final CourseResourceMapper resourceMapper;

    @Override
    @Transactional
    public CourseResourceVO createResource(Long courseId, CourseResourceDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser.getRole() != Role.TEACHER && currentUser.getRole() != Role.ADMIN) {
            throw new BusinessException(403, "Only teachers and admins can create resources");
        }
        CourseResourcePO po = resourceMapper.dtoToPo(dto);
        po.setCourseId(courseId);
        po.setCreatedBy(currentUser.getId());
        return resourceMapper.poToVo(resourceRepository.save(po));
    }

    @Override
    @Transactional
    public void deleteResource(Long resourceId) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser.getRole() != Role.TEACHER && currentUser.getRole() != Role.ADMIN) {
            throw new BusinessException(403, "Only teachers and admins can delete resources");
        }
        CourseResourcePO po = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new BusinessException(404, "Resource not found"));
        resourceRepository.delete(po);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResourceVO getResource(Long resourceId) {
        CourseResourcePO po = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new BusinessException(404, "Resource not found"));
        return resourceMapper.poToVo(po);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseResourceVO> listResources(Long courseId, Pageable pageable) {
        return resourceMapper.poPageToVoPage(resourceRepository.findByCourseId(courseId, pageable));
    }
}
