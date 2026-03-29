package com.coursebuddy.domain.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseAdminVoteVO {
    private Long courseId;
    private Long candidateUserId;
    private Long votes;
    private Long totalMembers;
    private Long threshold;
    private Boolean promotedToAdmin;
}
