package com.coursebuddy.aiassistant;

import com.coursebuddy.common.EntityMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AnswerMapper implements EntityMapper<Answer, AnswerResponse> {

    @Override
    public AnswerResponse toDto(Answer entity) {
        if (entity == null) return null;
        return AnswerResponse.builder()
                .id(entity.getId())
                .questionId(entity.getQuestionId())
                .content(entity.getContent())
                .source(entity.getSource())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @Override
    public Answer toEntity(AnswerResponse dto) {
        if (dto == null) return null;
        return Answer.builder()
                .id(dto.getId())
                .questionId(dto.getQuestionId())
                .content(dto.getContent())
                .source(dto.getSource())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    @Override
    public List<AnswerResponse> toDtoList(List<Answer> entities) {
        if (entities == null) return null;
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<Answer> toEntityList(List<AnswerResponse> dtos) {
        if (dtos == null) return null;
        return dtos.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
