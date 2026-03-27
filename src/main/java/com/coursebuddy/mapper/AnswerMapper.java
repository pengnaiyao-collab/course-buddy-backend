package com.coursebuddy.mapper;

import com.coursebuddy.domain.dto.AnswerDTO;
import com.coursebuddy.domain.po.AnswerPO;
import com.coursebuddy.domain.vo.AnswerVO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AnswerMapper {

    public AnswerPO dtoToPo(AnswerDTO dto) {
        if (dto == null) return null;
        return AnswerPO.builder()
                .content(dto.getContent())
                .source(dto.getSource())
                .build();
    }

    public AnswerVO poToVo(AnswerPO po) {
        if (po == null) return null;
        return AnswerVO.builder()
                .id(po.getId())
                .questionId(po.getQuestionId())
                .content(po.getContent())
                .source(po.getSource())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    public List<AnswerVO> poListToVoList(List<AnswerPO> list) {
        if (list == null) return null;
        return list.stream().map(this::poToVo).collect(Collectors.toList());
    }

    public Page<AnswerVO> poPageToVoPage(Page<AnswerPO> page) {
        return page.map(this::poToVo);
    }
}
