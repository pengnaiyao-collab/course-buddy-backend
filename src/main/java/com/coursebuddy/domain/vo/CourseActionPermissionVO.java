package com.coursebuddy.domain.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseActionPermissionVO {
    private String actionKey;
    private String actionName;
    private Boolean l1;
    private Boolean l2;
    private Boolean l3;
    private Boolean l4;
}
