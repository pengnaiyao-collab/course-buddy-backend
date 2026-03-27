package com.coursebuddy.knowledgebase;

import com.coursebuddy.common.EntityMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class KnowledgeItemMapper implements EntityMapper<KnowledgeItem, KnowledgeItemResponse> {

    @Override
    public KnowledgeItemResponse toDto(KnowledgeItem entity) {
        if (entity == null) return null;
        return KnowledgeItemResponse.builder()
                .id(entity.getId())
                .courseId(entity.getCourseId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .fileUrl(entity.getFileUrl())
                .fileType(entity.getFileType())
                .category(entity.getCategory())
                .tags(entity.getTags())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @Override
    public KnowledgeItem toEntity(KnowledgeItemResponse dto) {
        if (dto == null) return null;
        return KnowledgeItem.builder()
                .id(dto.getId())
                .courseId(dto.getCourseId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .fileUrl(dto.getFileUrl())
                .fileType(dto.getFileType())
                .category(dto.getCategory())
                .tags(dto.getTags())
                .createdBy(dto.getCreatedBy())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    public KnowledgeItem toEntityFromRequest(KnowledgeItemRequest request) {
        if (request == null) return null;
        return KnowledgeItem.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .fileUrl(request.getFileUrl())
                .fileType(request.getFileType())
                .category(request.getCategory())
                .tags(request.getTags())
                .build();
    }

    @Override
    public List<KnowledgeItemResponse> toDtoList(List<KnowledgeItem> entities) {
        if (entities == null) return null;
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<KnowledgeItem> toEntityList(List<KnowledgeItemResponse> dtos) {
        if (dtos == null) return null;
        return dtos.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
