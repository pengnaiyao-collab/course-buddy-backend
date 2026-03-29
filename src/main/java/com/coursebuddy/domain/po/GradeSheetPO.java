package com.coursebuddy.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("grade_sheets")
public class GradeSheetPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private Long studentId;
    @Builder.Default
    private Integer assignmentScore = 0;
    @Builder.Default
    private Integer participationScore = 0;
    @Builder.Default
    private Integer quizScore = 0;
    private Integer midtermScore;
    private Integer finalScore;
    @Builder.Default
    private Integer totalScore = 0;
    private String grade;
    private LocalDateTime gradeDate;
    private String comments;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
