package com.coursebuddy.controller;

import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.domain.dto.LessonDTO;
import com.coursebuddy.domain.vo.LessonVO;
import com.coursebuddy.service.ILessonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Lessons", description = "Course lesson management endpoints")
@RestController
@RequestMapping("/v1/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final ILessonService lessonService;

    @Operation(summary = "Create a lesson", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/courses/{courseId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<LessonVO> createLesson(@PathVariable Long courseId, @Valid @RequestBody LessonDTO dto) {
        return ApiResponse.success("Lesson created successfully", lessonService.createLesson(courseId, dto));
    }

    @Operation(summary = "List lessons for a course", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/courses/{courseId}")
    public ApiResponse<Page<LessonVO>> listLessons(
            @PathVariable Long courseId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(lessonService.listLessons(courseId, pageable));
    }

    @Operation(summary = "Get a lesson by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{lessonId}")
    public ApiResponse<LessonVO> getLesson(@PathVariable Long lessonId) {
        return ApiResponse.success(lessonService.getLesson(lessonId));
    }

    @Operation(summary = "Update a lesson", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{lessonId}")
    public ApiResponse<LessonVO> updateLesson(@PathVariable Long lessonId, @Valid @RequestBody LessonDTO dto) {
        return ApiResponse.success("Lesson updated successfully", lessonService.updateLesson(lessonId, dto));
    }

    @Operation(summary = "Delete a lesson", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{lessonId}")
    public ApiResponse<Void> deleteLesson(@PathVariable Long lessonId) {
        lessonService.deleteLesson(lessonId);
        return ApiResponse.success(null);
    }

    @Operation(summary = "Publish a lesson", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{lessonId}/publish")
    public ApiResponse<LessonVO> publishLesson(@PathVariable Long lessonId) {
        return ApiResponse.success("Lesson published", lessonService.publishLesson(lessonId));
    }

    @Operation(summary = "Reorder lessons", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/courses/{courseId}/reorder")
    public ApiResponse<Void> reorderLessons(@PathVariable Long courseId, @RequestBody List<Long> lessonIds) {
        lessonService.reorderLessons(courseId, lessonIds);
        return ApiResponse.success(null);
    }
}
