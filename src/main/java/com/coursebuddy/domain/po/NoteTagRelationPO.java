package com.coursebuddy.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 笔记与标签的多对多关联实体。
 *
 * <p>记录单篇笔记与单个标签的关联关系，同时维护关联的创建时间。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("note_tag_relations")
public class NoteTagRelationPO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long noteId;
    private Long tagId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
