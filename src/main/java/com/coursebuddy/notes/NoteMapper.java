package com.coursebuddy.notes;

import com.coursebuddy.common.EntityMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class NoteMapper implements EntityMapper<Note, NoteResponse> {

    @Override
    public NoteResponse toDto(Note entity) {
        if (entity == null) return null;
        return NoteResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .courseId(entity.getCourseId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .category(entity.getCategory())
                .tags(entity.getTags())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @Override
    public Note toEntity(NoteResponse dto) {
        if (dto == null) return null;
        return Note.builder()
                .id(dto.getId())
                .userId(dto.getUserId())
                .courseId(dto.getCourseId())
                .title(dto.getTitle())
                .content(dto.getContent())
                .category(dto.getCategory())
                .tags(dto.getTags())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    public Note toEntityFromRequest(NoteRequest request) {
        if (request == null) return null;
        return Note.builder()
                .courseId(request.getCourseId())
                .title(request.getTitle())
                .content(request.getContent())
                .category(request.getCategory())
                .tags(request.getTags())
                .build();
    }

    @Override
    public List<NoteResponse> toDtoList(List<Note> entities) {
        if (entities == null) return null;
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<Note> toEntityList(List<NoteResponse> dtos) {
        if (dtos == null) return null;
        return dtos.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
