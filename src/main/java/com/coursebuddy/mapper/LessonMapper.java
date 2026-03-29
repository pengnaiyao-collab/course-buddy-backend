package com.coursebuddy.mapper;

import com.coursebuddy.domain.dto.LessonDTO;
import com.coursebuddy.domain.po.LessonPO;
import com.coursebuddy.domain.vo.LessonVO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LessonMapper {

    public LessonPO dtoToPo(LessonDTO dto) {
        if (dto == null) return null;
        return LessonPO.builder()
                .courseId(dto.getCourseId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .content(dto.getContent())
                .lessonOrder(dto.getLessonOrder() != null ? dto.getLessonOrder() : 1)
                .duration(dto.getDuration() != null ? dto.getDuration() : 0)
                .videoUrl(dto.getVideoUrl())
                .resourceUrls(dto.getResourceUrls())
                .build();
    }

    public LessonVO poToVo(LessonPO po) {
        if (po == null) return null;
        return LessonVO.builder()
                .id(po.getId())
                .courseId(po.getCourseId())
                .title(po.getTitle())
                .description(po.getDescription())
                .content(po.getContent())
                .lessonOrder(po.getLessonOrder())
                .duration(po.getDuration())
                .videoUrl(po.getVideoUrl())
                .resourceUrls(po.getResourceUrls())
                .isPublished(po.getIsPublished())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    public List<LessonVO> poListToVoList(List<LessonPO> list) {
        if (list == null) return null;
        return list.stream().map(this::poToVo).collect(Collectors.toList());
    }

    public Page<LessonVO> poPageToVoPage(Page<LessonPO> page) {
        return page.map(this::poToVo);
    }
}
