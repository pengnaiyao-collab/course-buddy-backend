package com.coursebuddy.converter;

import com.coursebuddy.domain.po.AssignmentSubmissionPO;
import com.coursebuddy.domain.vo.AssignmentSubmissionVO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AssignmentSubmissionConverter {

    public AssignmentSubmissionVO poToVo(AssignmentSubmissionPO po) {
        if (po == null) return null;
        return AssignmentSubmissionVO.builder()
                .id(po.getId())
                .assignmentId(po.getAssignmentId())
                .studentId(po.getStudentId())
                .submissionUrl(po.getSubmissionUrl())
                .submittedAt(po.getSubmittedAt())
                .score(po.getScore())
                .feedback(po.getFeedback())
                .gradedAt(po.getGradedAt())
                .gradedBy(po.getGradedBy())
                .status(po.getStatus())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    public List<AssignmentSubmissionVO> poListToVoList(List<AssignmentSubmissionPO> list) {
        if (list == null) return null;
        return list.stream().map(this::poToVo).collect(Collectors.toList());
    }

    public Page<AssignmentSubmissionVO> poPageToVoPage(Page<AssignmentSubmissionPO> page) {
        return page.map(this::poToVo);
    }
}
