package com.coursebuddy.domain.dto;

import lombok.Data;

/**
 * 成绩提交传输对象
 */
@Data
public class GradeSubmissionDTO {
    private Double score;
    private String comment;
}
