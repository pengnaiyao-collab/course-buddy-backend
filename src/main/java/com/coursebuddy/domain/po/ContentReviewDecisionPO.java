package com.coursebuddy.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@TableName("content_review_decisions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentReviewDecisionPO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long reviewId;
    private Long reviewerId;
    private String decision;
    private String comments;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
