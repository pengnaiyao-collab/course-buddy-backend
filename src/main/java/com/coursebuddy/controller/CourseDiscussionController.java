package com.coursebuddy.controller;

import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.domain.dto.CourseDiscussionDTO;
import com.coursebuddy.domain.vo.CourseDiscussionVO;
import com.coursebuddy.service.ICourseDiscussionService;
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

/**
 * 课程讨论控制器
 */
@Tag(name = "Course Discussions", description = "Course discussion endpoints")
@RestController
@RequestMapping("/discussions")
@RequiredArgsConstructor
public class CourseDiscussionController {

    private final ICourseDiscussionService service;

    @Operation(summary = "Create a discussion post", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CourseDiscussionVO> create(@Valid @RequestBody CourseDiscussionDTO dto) {
        return ApiResponse.success("Discussion created", service.create(dto));
    }

    @Operation(summary = "List discussions for a course", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/courses/{courseId}")
    public ApiResponse<Page<CourseDiscussionVO>> listByCourse(
            @PathVariable Long courseId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(service.listByCourse(courseId, pageable));
    }

    @Operation(summary = "Get discussion by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}")
    public ApiResponse<CourseDiscussionVO> getById(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @Operation(summary = "Update a discussion", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}")
    public ApiResponse<CourseDiscussionVO> update(
            @PathVariable Long id,
            @Valid @RequestBody CourseDiscussionDTO dto) {
        return ApiResponse.success(service.update(id, dto));
    }

    @Operation(summary = "Delete a discussion", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "Pin/unpin a discussion", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{id}/pin")
    public ApiResponse<CourseDiscussionVO> pin(@PathVariable Long id) {
        return ApiResponse.success(service.pin(id));
    }
}
