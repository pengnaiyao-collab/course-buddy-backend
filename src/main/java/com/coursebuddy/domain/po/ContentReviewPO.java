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
@TableName("content_reviews")
public class ContentReviewPO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String contentType;
    private Long contentId;
    private Long reviewerId;
    private Long secondReviewerId;
    @Builder.Default
    private String status = "PENDING";
    @Builder.Default
    private Integer requiredApprovals = 2;
    @Builder.Default
    private Integer approvalCount = 0;
    @Builder.Default
    private String moderationStatus = "NORMAL";
    private String violationReason;
    private String comments;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
}
