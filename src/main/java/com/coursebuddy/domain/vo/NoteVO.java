package com.coursebuddy.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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
    /** 笔记摘要/描述。 */
    private String description;
    /** 笔记状态：DRAFT、PUBLISHED、ARCHIVED。 */
    private String status;
    private String category;
    private String tags;
    /** 关联标签 ID 列表。 */
    private List<Long> tagIds;
    /** 是否公开分享。 */
    private Boolean isPublic;
    /** 乐观锁版本号。 */
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
