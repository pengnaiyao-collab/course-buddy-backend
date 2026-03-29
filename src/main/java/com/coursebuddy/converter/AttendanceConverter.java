package com.coursebuddy.converter;

import com.coursebuddy.domain.po.AttendancePO;
import com.coursebuddy.domain.vo.AttendanceVO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AttendanceConverter {

    public AttendanceVO poToVo(AttendancePO po) {
        if (po == null) return null;
        return AttendanceVO.builder()
                .id(po.getId())
                .courseId(po.getCourseId())
                .studentId(po.getStudentId())
                .sessionDate(po.getSessionDate())
                .status(po.getStatus())
                .remarks(po.getRemarks())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    public List<AttendanceVO> poListToVoList(List<AttendancePO> list) {
        if (list == null) return null;
        return list.stream().map(this::poToVo).collect(Collectors.toList());
    }

    public Page<AttendanceVO> poPageToVoPage(Page<AttendancePO> page) {
        return page.map(this::poToVo);
    }
}
