package com.coursebuddy.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("knowledge_associations")
public class KnowledgeAssociationPO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sourceId;
    private Long targetId;
    @Builder.Default
    private String relationType = "RELATED";
    private String description;
    private Long createdBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
