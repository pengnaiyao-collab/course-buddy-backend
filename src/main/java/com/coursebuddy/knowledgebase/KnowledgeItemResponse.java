package com.coursebuddy.knowledgebase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeItemResponse {

    private Long id;
    private Long courseId;
    private String title;
    private String description;
    private String fileUrl;
    private String fileType;
    private String category;
    private String tags;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static KnowledgeItemResponse from(KnowledgeItem item) {
        return KnowledgeItemResponse.builder()
                .id(item.getId())
                .courseId(item.getCourseId())
                .title(item.getTitle())
                .description(item.getDescription())
                .fileUrl(item.getFileUrl())
                .fileType(item.getFileType())
                .category(item.getCategory())
                .tags(item.getTags())
                .createdBy(item.getCreatedBy())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
