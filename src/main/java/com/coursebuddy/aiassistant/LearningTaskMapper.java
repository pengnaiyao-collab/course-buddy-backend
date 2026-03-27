package com.coursebuddy.aiassistant;

import com.coursebuddy.common.EntityMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LearningTaskMapper implements EntityMapper<LearningTask, LearningTaskResponse> {

    @Override
    public LearningTaskResponse toDto(LearningTask entity) {
        if (entity == null) return null;
        return LearningTaskResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .courseId(entity.getCourseId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .dueDate(entity.getDueDate())
                .priority(entity.getPriority())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @Override
    public LearningTask toEntity(LearningTaskResponse dto) {
        if (dto == null) return null;
        return LearningTask.builder()
                .id(dto.getId())
                .userId(dto.getUserId())
                .courseId(dto.getCourseId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .status(dto.getStatus())
                .dueDate(dto.getDueDate())
                .priority(dto.getPriority())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    public LearningTask toEntityFromRequest(LearningTaskRequest request) {
        if (request == null) return null;
        return LearningTask.builder()
                .courseId(request.getCourseId())
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus())
                .dueDate(request.getDueDate())
                .priority(request.getPriority())
                .build();
    }

    @Override
    public List<LearningTaskResponse> toDtoList(List<LearningTask> entities) {
        if (entities == null) return null;
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<LearningTask> toEntityList(List<LearningTaskResponse> dtos) {
        if (dtos == null) return null;
        return dtos.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
