package com.coursebuddy.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeItemQueryDTO {
    private Long courseId;
    private String keyword;
    private String category;
    private int page = 0;
    private int size = 10;
}
