package com.coursebuddy.service.impl;

import com.coursebuddy.domain.auth.User;
import com.coursebuddy.common.MybatisPlusPageUtils;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.coursebuddy.domain.dto.CourseDiscussionDTO;
import com.coursebuddy.domain.po.CourseDiscussionPO;
import com.coursebuddy.domain.vo.CourseDiscussionVO;
import com.coursebuddy.converter.CourseDiscussionConverter;
import com.coursebuddy.mapper.CourseDiscussionMapper;
import com.coursebuddy.mapper.UserMapper;
import com.coursebuddy.domain.po.UserPO;
import com.coursebuddy.service.ICourseDiscussionService;
import com.coursebuddy.util.AccessControlValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 课程讨论服务实现
 */
@Service
@RequiredArgsConstructor
public class CourseDiscussionServiceImpl implements ICourseDiscussionService {

    private final CourseDiscussionMapper discussionRepository;
    private final CourseDiscussionConverter discussionMapper;
    private final AccessControlValidator accessControlValidator;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public CourseDiscussionVO create(CourseDiscussionDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        // 验证用户是否是课程成员
        accessControlValidator.validateCourseMember(dto.getCourseId(), currentUser.getId());
        
        CourseDiscussionPO po = discussionMapper.dtoToPo(dto);
        po.setAuthorId(currentUser.getId());
        discussionRepository.insert(po);
        CourseDiscussionVO vo = discussionMapper.poToVo(po);
        vo.setAuthorName(resolveUserName(currentUser.getId()));
        return vo;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseDiscussionVO> listByCourse(Long courseId, Pageable pageable) {
        IPage<CourseDiscussionPO> poPage = discussionRepository.findByCourseIdAndParentIdIsNullAndIsDeletedFalse(
                MybatisPlusPageUtils.toMpPage(pageable), courseId);
        Page<CourseDiscussionVO> page = discussionMapper.poPageToVoPage(
                MybatisPlusPageUtils.toSpringPage(poPage, pageable));
        page.forEach(vo -> {
            List<CourseDiscussionPO> replies = discussionRepository
                    .findByParentIdAndIsDeletedFalseOrderByCreatedAtAsc(vo.getId());
            vo.setReplies(discussionMapper.poListToVoList(replies));
        });
        attachAuthorNames(page.getContent());
        page.forEach(vo -> attachAuthorNames(vo.getReplies()));
        return page;
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDiscussionVO getById(Long id) {
        CourseDiscussionPO po = discussionRepository.selectById(id);
        if (po == null) {
            throw new BusinessException(404, "Discussion not found");
        }
        CourseDiscussionVO vo = discussionMapper.poToVo(po);
        List<CourseDiscussionPO> replies = discussionRepository
                .findByParentIdAndIsDeletedFalseOrderByCreatedAtAsc(id);
        vo.setReplies(discussionMapper.poListToVoList(replies));
        attachAuthorNames(List.of(vo));
        attachAuthorNames(vo.getReplies());
        return vo;
    }

    @Override
    @Transactional
    public CourseDiscussionVO update(Long id, CourseDiscussionDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        CourseDiscussionPO po = discussionRepository.selectById(id);
        if (po == null) {
            throw new BusinessException(404, "Discussion not found");
        }
        if (!po.getAuthorId().equals(currentUser.getId())) {
            throw new BusinessException(403, "You are not authorized to update this discussion");
        }
        if (dto.getTitle() != null) po.setTitle(dto.getTitle());
        if (dto.getContent() != null) po.setContent(dto.getContent());
        discussionRepository.updateById(po);
        CourseDiscussionVO vo = discussionMapper.poToVo(po);
        vo.setAuthorName(resolveUserName(currentUser.getId()));
        return vo;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        CourseDiscussionPO po = discussionRepository.selectById(id);
        if (po == null) {
            throw new BusinessException(404, "Discussion not found");
        }
        if (!po.getAuthorId().equals(currentUser.getId())) {
            throw new BusinessException(403, "You are not authorized to delete this discussion");
        }
        po.setIsDeleted(true);
        discussionRepository.updateById(po);
    }

    @Override
    @Transactional
    public CourseDiscussionVO pin(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        CourseDiscussionPO po = discussionRepository.selectById(id);
        if (po == null) {
            throw new BusinessException(404, "Discussion not found");
        }
        // 验证用户是否是课程讲师（只有讲师才能置顶讨论）
        accessControlValidator.validateCourseInstructor(po.getCourseId(), currentUser.getId());
        
        po.setIsPinned(!po.getIsPinned());
        discussionRepository.updateById(po);
        return discussionMapper.poToVo(po);
    }

    private void attachAuthorNames(List<CourseDiscussionVO> discussions) {
        if (discussions == null || discussions.isEmpty()) {
            return;
        }
        List<Long> userIds = discussions.stream()
                .map(CourseDiscussionVO::getAuthorId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, UserPO> usersById = userMapper.selectBatchIds(userIds)
                .stream()
                .collect(Collectors.toMap(UserPO::getId, user -> user));
        for (CourseDiscussionVO discussion : discussions) {
            UserPO user = usersById.get(discussion.getAuthorId());
            discussion.setAuthorName(resolveUserName(user));
        }
    }

    private String resolveUserName(Long userId) {
        UserPO user = userMapper.selectById(userId);
        return resolveUserName(user);
    }

    private String resolveUserName(UserPO user) {
        if (user == null) {
            return "匿名";
        }
        if (user.getRealName() != null && !user.getRealName().isBlank()) {
            return user.getRealName();
        }
        return user.getUsername();
    }
}
