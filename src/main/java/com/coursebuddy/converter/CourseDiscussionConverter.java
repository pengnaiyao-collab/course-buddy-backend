package com.coursebuddy.converter;

import com.coursebuddy.domain.dto.CourseDiscussionDTO;
import com.coursebuddy.domain.po.CourseDiscussionPO;
import com.coursebuddy.domain.vo.CourseDiscussionVO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CourseDiscussionConverter {

    public CourseDiscussionPO dtoToPo(CourseDiscussionDTO dto) {
        if (dto == null) return null;
        return CourseDiscussionPO.builder()
                .courseId(dto.getCourseId())
                .parentId(dto.getParentId())
                .title(dto.getTitle())
                .content(dto.getContent())
                .build();
    }

    public CourseDiscussionVO poToVo(CourseDiscussionPO po) {
        if (po == null) return null;
        return CourseDiscussionVO.builder()
                .id(po.getId())
                .courseId(po.getCourseId())
                .parentId(po.getParentId())
                .authorId(po.getAuthorId())
                .title(po.getTitle())
                .content(po.getContent())
                .likeCount(po.getLikeCount())
                .isPinned(po.getIsPinned())
                .likedByMe(false)
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    public List<CourseDiscussionVO> poListToVoList(List<CourseDiscussionPO> list) {
        if (list == null) return null;
        return list.stream().map(this::poToVo).collect(Collectors.toList());
    }

    public Page<CourseDiscussionVO> poPageToVoPage(Page<CourseDiscussionPO> page) {
        return page.map(this::poToVo);
    }
}
