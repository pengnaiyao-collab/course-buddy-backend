package com.coursebuddy.domain.vo;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class GradeSheetVO {
    private Long id;
    private Long courseId;
    private Long studentId;
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
