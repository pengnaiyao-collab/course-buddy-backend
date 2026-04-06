package com.coursebuddy.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 笔记视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteVO {

    private Long id;
    private Long userId;
    private Long courseId;
    /** 所属分类 ID。 */
    private Long categoryId;
    private String title;
    private String content;
    private String category;
    /** 附件链接列表。 */
    private List<String> attachments;
    /** 是否公开分享。 */
    private Boolean isPublic;
    /** 乐观锁版本号。 */
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
