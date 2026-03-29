package com.coursebuddy.service.impl;

import com.coursebuddy.auth.User;
import com.coursebuddy.common.MybatisPlusPageUtils;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.coursebuddy.domain.dto.CourseDiscussionDTO;
import com.coursebuddy.domain.po.CourseDiscussionPO;
import com.coursebuddy.domain.vo.CourseDiscussionVO;
import com.coursebuddy.converter.CourseDiscussionConverter;
import com.coursebuddy.mapper.CourseDiscussionMapper;
import com.coursebuddy.service.ICourseDiscussionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseDiscussionServiceImpl implements ICourseDiscussionService {

    private final CourseDiscussionMapper discussionRepository;
    private final CourseDiscussionConverter discussionMapper;

    @Override
    @Transactional
    public CourseDiscussionVO create(CourseDiscussionDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        CourseDiscussionPO po = discussionMapper.dtoToPo(dto);
        po.setAuthorId(currentUser.getId());
        discussionRepository.insert(po);
        return discussionMapper.poToVo(po);
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
        return discussionMapper.poToVo(po);
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
    public CourseDiscussionVO like(Long id) {
        CourseDiscussionPO po = discussionRepository.selectById(id);
        if (po == null) {
            throw new BusinessException(404, "Discussion not found");
        }
        discussionRepository.incrementLikeCount(id);
        po.setLikeCount(po.getLikeCount() + 1);
        return discussionMapper.poToVo(po);
    }

    @Override
    @Transactional
    public CourseDiscussionVO pin(Long id) {
        CourseDiscussionPO po = discussionRepository.selectById(id);
        if (po == null) {
            throw new BusinessException(404, "Discussion not found");
        }
        po.setIsPinned(!po.getIsPinned());
        discussionRepository.updateById(po);
        return discussionMapper.poToVo(po);
    }
}
