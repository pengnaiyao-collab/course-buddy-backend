package com.coursebuddy.converter;

import com.coursebuddy.domain.dto.QuestionDTO;
import com.coursebuddy.domain.po.QuestionPO;
import com.coursebuddy.domain.vo.QuestionVO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class QuestionConverter {

    public QuestionPO dtoToPo(QuestionDTO dto) {
        if (dto == null) return null;
        return QuestionPO.builder()
                .courseId(dto.getCourseId())
                .content(dto.getContent())
                .subject(dto.getSubject())
                .build();
    }

    public QuestionVO poToVo(QuestionPO po) {
        if (po == null) return null;
        return QuestionVO.builder()
                .id(po.getId())
                .courseId(po.getCourseId())
                .userId(po.getUserId())
                .content(po.getContent())
                .subject(po.getSubject())
                .status(po.getStatus())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    public List<QuestionVO> poListToVoList(List<QuestionPO> list) {
        if (list == null) return null;
        return list.stream().map(this::poToVo).collect(Collectors.toList());
    }

    public Page<QuestionVO> poPageToVoPage(Page<QuestionPO> page) {
        return page.map(this::poToVo);
    }
}
