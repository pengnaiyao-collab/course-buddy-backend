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
@TableName("ai_conversations")
public class ConversationPO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    @Builder.Default
    private String model = "generalv3.5";
    @Builder.Default
    private String status = "ACTIVE";
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
