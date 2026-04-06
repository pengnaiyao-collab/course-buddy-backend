package com.coursebuddy.domain.vo;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 成绩成绩单视图对象
 */
@Data
@Builder
public class GradeSheetVO {
    private Long id;
    private Long courseId;
    private Long studentId;
    private String studentName;
    private Integer assignmentScore;
    private Integer participationScore;
    private Integer quizScore;
    private Integer midtermScore;
    private Integer finalScore;
    private Integer totalScore;
    private String grade;
    private LocalDateTime gradeDate;
    private String comments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
