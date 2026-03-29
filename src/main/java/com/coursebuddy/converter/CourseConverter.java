package com.coursebuddy.converter;

import com.coursebuddy.domain.dto.CourseDTO;
import com.coursebuddy.domain.po.CoursePO;
import com.coursebuddy.domain.vo.CourseVO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CourseConverter {

    public CoursePO dtoToPo(CourseDTO dto) {
        if (dto == null) return null;
        return CoursePO.builder()
                .code(dto.getCode())
                .name(dto.getName())
                .description(dto.getDescription())
                .creditHours(dto.getCreditHours() != null ? dto.getCreditHours() : 3)
                .level(dto.getLevel() != null ? dto.getLevel() : "BEGINNER")
                .capacity(dto.getCapacity() != null ? dto.getCapacity() : 30)
                .thumbnailUrl(dto.getThumbnailUrl())
                .syllabus(dto.getSyllabus())
                .maxGrade(dto.getMaxGrade() != null ? dto.getMaxGrade() : 100)
                .passingGrade(dto.getPassingGrade() != null ? dto.getPassingGrade() : 60)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .departmentId(dto.getDepartmentId())
                .build();
    }

    public CourseVO poToVo(CoursePO po) {
        if (po == null) return null;
        return CourseVO.builder()
                .id(po.getId())
                .code(po.getCode())
                .name(po.getName())
                .description(po.getDescription())
                .instructorId(po.getInstructorId())
                .departmentId(po.getDepartmentId())
                .creditHours(po.getCreditHours())
                .level(po.getLevel())
                .capacity(po.getCapacity())
                .enrolledCount(po.getEnrolledCount())
                .thumbnailUrl(po.getThumbnailUrl())
                .syllabus(po.getSyllabus())
                .maxGrade(po.getMaxGrade())
                .passingGrade(po.getPassingGrade())
                .status(po.getStatus())
                .startDate(po.getStartDate())
                .endDate(po.getEndDate())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    public List<CourseVO> poListToVoList(List<CoursePO> list) {
        if (list == null) return null;
        return list.stream().map(this::poToVo).collect(Collectors.toList());
    }

    public Page<CourseVO> poPageToVoPage(Page<CoursePO> page) {
        return page.map(this::poToVo);
    }
}
