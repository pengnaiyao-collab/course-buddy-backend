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
@TableName("ai_usage_stats")
public class AiUsageStatsPO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String model;

    /** 请求类型: CHAT, GENERATE_OUTLINE, GENERATE_QUESTIONS, GENERATE_POINTS, GENERATE_BREAKDOWN */
    private String requestType;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;

    /** 请求耗时（毫秒） */
    private Long durationMs;

    /** 状态: SUCCESS, FAILED */
    private String status;
    private String errorMessage;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
