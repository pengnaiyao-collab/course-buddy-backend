package com.coursebuddy.collaboration;

import com.coursebuddy.common.EntityMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CollaborationProjectMapper implements EntityMapper<CollaborationProject, ProjectResponse> {

    @Override
    public ProjectResponse toDto(CollaborationProject entity) {
        if (entity == null) return null;
        return ProjectResponse.builder()
                .id(entity.getId())
                .courseId(entity.getCourseId())
                .name(entity.getName())
                .description(entity.getDescription())
                .ownerId(entity.getOwnerId())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @Override
    public CollaborationProject toEntity(ProjectResponse dto) {
        if (dto == null) return null;
        return CollaborationProject.builder()
                .id(dto.getId())
                .courseId(dto.getCourseId())
                .name(dto.getName())
                .description(dto.getDescription())
                .ownerId(dto.getOwnerId())
                .status(dto.getStatus())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    public CollaborationProject toEntityFromRequest(ProjectRequest request) {
        if (request == null) return null;
        return CollaborationProject.builder()
                .courseId(request.getCourseId())
                .name(request.getName())
                .description(request.getDescription())
                .status(request.getStatus())
                .build();
    }

    @Override
    public List<ProjectResponse> toDtoList(List<CollaborationProject> entities) {
        if (entities == null) return null;
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<CollaborationProject> toEntityList(List<ProjectResponse> dtos) {
        if (dtos == null) return null;
        return dtos.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
