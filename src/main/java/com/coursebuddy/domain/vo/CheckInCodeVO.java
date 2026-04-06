package com.coursebuddy.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInCodeVO {
    private String code;
    private Long remainingSeconds;
}
