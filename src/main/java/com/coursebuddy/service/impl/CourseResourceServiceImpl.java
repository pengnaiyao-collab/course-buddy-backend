package com.coursebuddy.service.impl;

import com.coursebuddy.domain.auth.Role;
import com.coursebuddy.domain.auth.User;
import com.coursebuddy.common.MybatisPlusPageUtils;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.coursebuddy.domain.dto.CourseResourceDTO;
import com.coursebuddy.domain.po.CourseResourcePO;
import com.coursebuddy.domain.vo.CourseResourceVO;
import com.coursebuddy.converter.CourseResourceConverter;
import com.coursebuddy.mapper.CourseResourceMapper;
import com.coursebuddy.mapper.UserMapper;
import com.coursebuddy.domain.po.UserPO;
import com.coursebuddy.service.ICourseResourceService;
import com.coursebuddy.util.AccessControlValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程资源服务实现
 */
@Service
@RequiredArgsConstructor
public class CourseResourceServiceImpl implements ICourseResourceService {

    private final CourseResourceMapper resourceRepository;
    private final CourseResourceConverter resourceMapper;
    private final AccessControlValidator accessControlValidator;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public CourseResourceVO createResource(Long courseId, CourseResourceDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser.getRole() != Role.TEACHER && currentUser.getRole() != Role.TA) {
            throw new BusinessException(403, "Only teachers and TAs can create resources");
        }
        CourseResourcePO po = resourceMapper.dtoToPo(dto);
        po.setCourseId(courseId);
        po.setCreatedBy(currentUser.getId());
        resourceRepository.insert(po);
        return resourceMapper.poToVo(po);
    }

    @Override
    @Transactional
    public void deleteResource(Long resourceId) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser.getRole() != Role.TEACHER && currentUser.getRole() != Role.TA) {
            throw new BusinessException(403, "Only teachers and TAs can delete resources");
        }
        CourseResourcePO po = resourceRepository.selectById(resourceId);
        if (po == null) {
            throw new BusinessException(404, "Resource not found");
        }
        // 验证资源所属课程是否属于当前讲师
        accessControlValidator.validateCourseInstructor(po.getCourseId(), currentUser.getId());
        
        resourceRepository.deleteById(po.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResourceVO getResource(Long resourceId) {
        CourseResourcePO po = resourceRepository.selectById(resourceId);
        if (po == null) {
            throw new BusinessException(404, "Resource not found");
        }
        return resourceMapper.poToVo(po);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseResourceVO> listResources(Long courseId, Pageable pageable) {
        IPage<CourseResourcePO> poPage = resourceRepository.findByCourseId(
                MybatisPlusPageUtils.toMpPage(pageable), courseId);
        Page<CourseResourceVO> voPage = resourceMapper.poPageToVoPage(MybatisPlusPageUtils.toSpringPage(poPage, pageable));
        attachUploaderNames(voPage.getContent());
        return voPage;
    }

    private void attachUploaderNames(List<CourseResourceVO> resources) {
        if (resources == null || resources.isEmpty()) {
            return;
        }
        List<Long> userIds = resources.stream()
                .map(CourseResourceVO::getCreatedBy)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, UserPO> usersById = userMapper.selectBatchIds(userIds)
                .stream()
                .collect(Collectors.toMap(UserPO::getId, user -> user));
        for (CourseResourceVO resource : resources) {
            UserPO user = usersById.get(resource.getCreatedBy());
            resource.setUploaderName(resolveUserName(user));
        }
    }

    private String resolveUserName(UserPO user) {
        if (user == null) {
            return "";
        }
        if (user.getRealName() != null && !user.getRealName().isBlank()) {
            return user.getRealName();
        }
        return user.getUsername();
    }
}
