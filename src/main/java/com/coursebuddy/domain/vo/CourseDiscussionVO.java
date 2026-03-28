package com.coursebuddy.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDiscussionVO {
    private Long id;
    private Long courseId;
    private Long parentId;
    private Long authorId;
    private String authorName;
    private String title;
    private String content;
    private Integer likeCount;
    private Boolean isPinned;
    private Boolean likedByMe;
    private List<CourseDiscussionVO> replies;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
