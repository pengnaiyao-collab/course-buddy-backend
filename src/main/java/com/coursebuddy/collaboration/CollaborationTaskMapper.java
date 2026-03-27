package com.coursebuddy.collaboration;

import com.coursebuddy.common.EntityMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CollaborationTaskMapper implements EntityMapper<CollaborationTask, TaskResponse> {

    @Override
    public TaskResponse toDto(CollaborationTask entity) {
        if (entity == null) return null;
        return TaskResponse.builder()
                .id(entity.getId())
                .projectId(entity.getProjectId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .assigneeId(entity.getAssigneeId())
                .status(entity.getStatus())
                .priority(entity.getPriority())
                .dueDate(entity.getDueDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @Override
    public CollaborationTask toEntity(TaskResponse dto) {
        if (dto == null) return null;
        return CollaborationTask.builder()
                .id(dto.getId())
                .projectId(dto.getProjectId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .assigneeId(dto.getAssigneeId())
                .status(dto.getStatus())
                .priority(dto.getPriority())
                .dueDate(dto.getDueDate())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    public CollaborationTask toEntityFromRequest(TaskRequest request) {
        if (request == null) return null;
        return CollaborationTask.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .assigneeId(request.getAssigneeId())
                .status(request.getStatus())
                .priority(request.getPriority())
                .dueDate(request.getDueDate())
                .build();
    }

    @Override
    public List<TaskResponse> toDtoList(List<CollaborationTask> entities) {
        if (entities == null) return null;
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<CollaborationTask> toEntityList(List<TaskResponse> dtos) {
        if (dtos == null) return null;
        return dtos.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
