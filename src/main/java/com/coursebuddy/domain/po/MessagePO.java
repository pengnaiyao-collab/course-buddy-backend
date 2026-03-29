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
@TableName("messages")
public class MessagePO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String content;
    @Builder.Default
    private String msgType = "TEXT";
    @Builder.Default
    private Boolean isRead = false;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
