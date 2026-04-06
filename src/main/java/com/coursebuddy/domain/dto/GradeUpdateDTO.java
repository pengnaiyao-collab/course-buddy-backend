package com.coursebuddy.domain.dto;

import lombok.Data;

/**
 * 成绩传输对象
 */
@Data
public class GradeUpdateDTO {
    private Integer assignmentScore;
    private Integer participationScore;
    private Integer quizScore;
    private Integer midtermScore;
    private Integer finalScore;
    private String comments;
}
