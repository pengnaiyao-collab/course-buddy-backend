package com.coursebuddy.collaboration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {

    private Long id;
    private Long courseId;
    private String name;
    private String description;
    private Long ownerId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProjectResponse from(CollaborationProject project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .courseId(project.getCourseId())
                .name(project.getName())
                .description(project.getDescription())
                .ownerId(project.getOwnerId())
                .status(project.getStatus())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}
