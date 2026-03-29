package com.coursebuddy.converter;

import com.coursebuddy.domain.po.GeneratedContentPO;
import com.coursebuddy.domain.vo.GeneratedContentVO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GeneratedContentConverter {

    public GeneratedContentVO poToVo(GeneratedContentPO po) {
        if (po == null) return null;
        return GeneratedContentVO.builder()
                .id(po.getId())
                .userId(po.getUserId())
                .contentType(po.getContentType())
                .subject(po.getSubject())
                .prompt(po.getPrompt())
                .content(po.getContent())
                .courseId(po.getCourseId())
                .status(po.getStatus())
                .tokenCount(po.getTokenCount())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    public List<GeneratedContentVO> poListToVoList(List<GeneratedContentPO> list) {
        if (list == null) return null;
        return list.stream().map(this::poToVo).collect(Collectors.toList());
    }

    public Page<GeneratedContentVO> poPageToVoPage(Page<GeneratedContentPO> page) {
        return page.map(this::poToVo);
    }
}
