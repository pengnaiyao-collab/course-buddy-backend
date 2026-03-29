package com.coursebuddy.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeAnalyzeResultVO {
    private Integer totalCreated;
    private Integer splitParts;
    private Integer extractedSummaries;
    private List<KnowledgeItemVO> items;
}
