package com.coursebuddy.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@TableName("course_admin_votes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseAdminVotePO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private Long candidateUserId;
    private Long voterUserId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
