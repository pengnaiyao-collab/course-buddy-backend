package com.coursebuddy.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteVO {

    private Long id;
    private Long userId;
    private Long courseId;
    private String title;
    private String content;
    private String category;
    private String tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
