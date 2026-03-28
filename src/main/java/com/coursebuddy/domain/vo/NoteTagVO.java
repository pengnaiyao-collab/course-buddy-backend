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
public class NoteTagVO {
    private Long id;
    private Long userId;
    private String name;
    private String color;
    private Integer useCount;
    private LocalDateTime createdAt;
}
