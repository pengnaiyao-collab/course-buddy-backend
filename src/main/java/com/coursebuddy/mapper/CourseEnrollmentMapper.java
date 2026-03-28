package com.coursebuddy.mapper;

import com.coursebuddy.domain.po.CourseEnrollmentPO;
import com.coursebuddy.domain.vo.CourseEnrollmentVO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CourseEnrollmentMapper {

    public CourseEnrollmentVO poToVo(CourseEnrollmentPO po) {
        if (po == null) return null;
        return CourseEnrollmentVO.builder()
                .id(po.getId())
                .courseId(po.getCourseId())
                .userId(po.getUserId())
                .status(po.getStatus())
                .enrolledAt(po.getEnrolledAt())
                .droppedAt(po.getDroppedAt())
                .completedAt(po.getCompletedAt())
                .build();
    }

    public List<CourseEnrollmentVO> poListToVoList(List<CourseEnrollmentPO> list) {
        if (list == null) return null;
        return list.stream().map(this::poToVo).collect(Collectors.toList());
    }

    public Page<CourseEnrollmentVO> poPageToVoPage(Page<CourseEnrollmentPO> page) {
        return page.map(this::poToVo);
    }
}
