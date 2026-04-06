package com.coursebuddy.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 笔记分类视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteCategoryVO {
    private Long id;
    private Long userId;
    private String name;
    /** 分类描述。 */
    private String description;
    private String color;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
