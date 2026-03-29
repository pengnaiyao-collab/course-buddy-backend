package com.coursebuddy.converter;

import com.coursebuddy.domain.po.GradeSheetPO;
import com.coursebuddy.domain.vo.GradeSheetVO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GradeSheetConverter {

    public GradeSheetVO poToVo(GradeSheetPO po) {
        if (po == null) return null;
        return GradeSheetVO.builder()
                .id(po.getId())
                .courseId(po.getCourseId())
                .studentId(po.getStudentId())
                .assignmentScore(po.getAssignmentScore())
                .participationScore(po.getParticipationScore())
                .quizScore(po.getQuizScore())
                .midtermScore(po.getMidtermScore())
                .finalScore(po.getFinalScore())
                .totalScore(po.getTotalScore())
                .grade(po.getGrade())
                .gradeDate(po.getGradeDate())
                .comments(po.getComments())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    public List<GradeSheetVO> poListToVoList(List<GradeSheetPO> list) {
        if (list == null) return null;
        return list.stream().map(this::poToVo).collect(Collectors.toList());
    }

    public Page<GradeSheetVO> poPageToVoPage(Page<GradeSheetPO> page) {
        return page.map(this::poToVo);
    }
}
