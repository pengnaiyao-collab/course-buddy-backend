package com.coursebuddy.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeAssociationDTO {

    @NotNull(message = "目标知识点ID不能为空")
    private Long targetId;

    /** Relation type: RELATED, DERIVED_FROM, SUPPLEMENTS, CONFLICTS_WITH */
    @Size(max = 64, message = "关联类型长度不能超过64个字符")
    private String relationType = "RELATED";

    @Size(max = 500, message = "描述长度不能超过500个字符")
    private String description;
}
