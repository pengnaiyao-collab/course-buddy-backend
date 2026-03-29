package com.coursebuddy.converter;

import com.coursebuddy.domain.dto.CourseResourceDTO;
import com.coursebuddy.domain.po.CourseResourcePO;
import com.coursebuddy.domain.vo.CourseResourceVO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CourseResourceConverter {

    public CourseResourcePO dtoToPo(CourseResourceDTO dto) {
        if (dto == null) return null;
        return CourseResourcePO.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .resourceType(dto.getResourceType() != null ? dto.getResourceType() : "OTHER")
                .resourceUrl(dto.getResourceUrl())
                .fileSize(dto.getFileSize())
                .build();
    }

    public CourseResourceVO poToVo(CourseResourcePO po) {
        if (po == null) return null;
        return CourseResourceVO.builder()
                .id(po.getId())
                .courseId(po.getCourseId())
                .title(po.getTitle())
                .description(po.getDescription())
                .resourceType(po.getResourceType())
                .resourceUrl(po.getResourceUrl())
                .fileSize(po.getFileSize())
                .downloadCount(po.getDownloadCount())
                .createdBy(po.getCreatedBy())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    public List<CourseResourceVO> poListToVoList(List<CourseResourcePO> list) {
        if (list == null) return null;
        return list.stream().map(this::poToVo).collect(Collectors.toList());
    }

    public Page<CourseResourceVO> poPageToVoPage(Page<CourseResourcePO> page) {
        return page.map(this::poToVo);
    }
}
