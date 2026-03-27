package com.coursebuddy.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GenerateContentDTO {

    /**
     * 生成类型:
     * OUTLINE     - 复习提纲
     * EXAM_POINTS - 考点汇总
     * QUESTIONS   - 习题生成
     * BREAKDOWN   - 知识点拆解
     */
    @NotBlank(message = "生成类型不能为空")
    private String contentType;

    /** 主题或知识点 */
    @NotBlank(message = "主题不能为空")
    private String subject;

    /** 关联课程 ID（可选） */
    private Long courseId;

    /** 额外要求或说明（可选） */
    private String requirements;

    /** 难度级别: EASY, MEDIUM, HARD（仅 QUESTIONS 类型有效） */
    private String difficulty;

    /** 生成数量（仅 QUESTIONS 类型有效，默认 5） */
    private Integer count = 5;
}
