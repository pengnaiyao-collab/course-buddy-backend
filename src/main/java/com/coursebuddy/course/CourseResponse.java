package com.coursebuddy.course;

import com.coursebuddy.auth.User;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CourseResponse {

    private Long id;
    private String title;
    private String description;
    private Long teacherId;
    private String teacherName;
    private BigDecimal price;
    private String category;
    private CourseStatus status;
    private Integer maxStudents;
    private String coverImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CourseResponse from(Course course, User teacher) {
        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .teacherId(course.getTeacherId())
                .teacherName(teacher != null ? teacher.getFullName() : null)
                .price(course.getPrice())
                .category(course.getCategory())
                .status(course.getStatus())
                .maxStudents(course.getMaxStudents())
                .coverImageUrl(course.getCoverImageUrl())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
}
