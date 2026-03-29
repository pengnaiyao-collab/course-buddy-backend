package com.coursebuddy.domain.po;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "grade_sheets", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"course_id", "student_id"})
})
public class GradeSheetPO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "assignment_score")
    @Builder.Default
    private Integer assignmentScore = 0;

    @Column(name = "participation_score")
    @Builder.Default
    private Integer participationScore = 0;

    @Column(name = "quiz_score")
    @Builder.Default
    private Integer quizScore = 0;

    @Column(name = "midterm_score")
    private Integer midtermScore;

    @Column(name = "final_score")
    private Integer finalScore;

    @Column(name = "total_score")
    @Builder.Default
    private Integer totalScore = 0;

    @Column(length = 4)
    private String grade;

    @Column(name = "grade_date")
    private LocalDateTime gradeDate;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
