package com.coursebuddy.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册响应视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponseVO {

    private UserVO user;
    private String message;
}
