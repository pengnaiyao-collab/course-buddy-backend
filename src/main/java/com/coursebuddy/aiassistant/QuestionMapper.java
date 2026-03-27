package com.coursebuddy.aiassistant;

import com.coursebuddy.common.EntityMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class QuestionMapper implements EntityMapper<Question, QuestionResponse> {

    @Override
    public QuestionResponse toDto(Question entity) {
        if (entity == null) return null;
        return QuestionResponse.builder()
                .id(entity.getId())
                .courseId(entity.getCourseId())
                .userId(entity.getUserId())
                .content(entity.getContent())
                .subject(entity.getSubject())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @Override
    public Question toEntity(QuestionResponse dto) {
        if (dto == null) return null;
        return Question.builder()
                .id(dto.getId())
                .courseId(dto.getCourseId())
                .userId(dto.getUserId())
                .content(dto.getContent())
                .subject(dto.getSubject())
                .status(dto.getStatus())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    public Question toEntityFromRequest(QuestionRequest request) {
        if (request == null) return null;
        return Question.builder()
                .courseId(request.getCourseId())
                .content(request.getContent())
                .subject(request.getSubject())
                .build();
    }

    @Override
    public List<QuestionResponse> toDtoList(List<Question> entities) {
        if (entities == null) return null;
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<Question> toEntityList(List<QuestionResponse> dtos) {
        if (dtos == null) return null;
        return dtos.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
