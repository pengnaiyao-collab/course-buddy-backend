package com.coursebuddy.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerVO {

    private Long id;
    private Long questionId;
    private String content;
    private String source;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
