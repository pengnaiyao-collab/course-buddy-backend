package com.coursebuddy.notes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteResponse {

    private Long id;
    private Long userId;
    private Long courseId;
    private String title;
    private String content;
    private String category;
    private String tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NoteResponse from(Note note) {
        return NoteResponse.builder()
                .id(note.getId())
                .userId(note.getUserId())
                .courseId(note.getCourseId())
                .title(note.getTitle())
                .content(note.getContent())
                .category(note.getCategory())
                .tags(note.getTags())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}
