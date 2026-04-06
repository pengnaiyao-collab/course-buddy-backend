package com.coursebuddy.domain.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 知识库视图对象
 */
@Data
@Builder
public class KnowledgeSourceVO {

    private Long knowledgeItemId;
    private String title;
    private String snippet;
    private String sourceType;
}
