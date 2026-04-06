package com.coursebuddy.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 笔记持久化对象。
 *
 * <p>使用 JPA 乐观锁（{@code @Version}）防止并发更新冲突。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "notes", autoResultMap = true)
public class NotePO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 乐观锁版本号，由 JPA 自动维护。
     */
    @Builder.Default
    private Long optLockVersion = 0L;
    private Long userId;
    private Long courseId;

    /** 所属分类 ID（外键，可选）。 */
    private Long categoryId;
    private String title;
    private String content;

    private String category;
    
    /** 附件 URL 列表（JSON 格式）。 */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private List<String> attachments;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 是否公开（可被分享链接访问）。 */
    @Builder.Default
    private Boolean isPublic = false;

    /** 软删除标志。 */
    @Builder.Default
    private Boolean isDeleted = false;

    /** 软删除时间。 */
    @TableLogic
    private LocalDateTime deletedAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
