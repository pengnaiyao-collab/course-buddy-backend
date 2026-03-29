package com.coursebuddy.domain.dto;

import lombok.Data;

@Data
public class GradeUpdateDTO {
    private Integer assignmentScore;
    private Integer participationScore;
    private Integer quizScore;
    private Integer midtermScore;
    private Integer finalScore;
    private String comments;
}
