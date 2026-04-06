package com.coursebuddy.controller;

import com.coursebuddy.common.response.ApiResponse;
import com.coursebuddy.domain.dto.CourseResourceDTO;
import com.coursebuddy.domain.vo.CourseResourceVO;
import com.coursebuddy.service.ICourseResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 课程资源控制器
 */
@Tag(name = "Course Resources", description = "Course resource management endpoints")
@RestController
@RequestMapping("/v1/course-resources")
@RequiredArgsConstructor
public class CourseResourceController {

    private final ICourseResourceService resourceService;

    @Operation(summary = "Create a course resource", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/courses/{courseId}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('TEACHER', 'TA')")
    public ApiResponse<CourseResourceVO> createResource(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseResourceDTO dto) {
        return ApiResponse.success("Resource created", resourceService.createResource(courseId, dto));
    }

    @Operation(summary = "List resources for a course", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/courses/{courseId}")
    public ApiResponse<Page<CourseResourceVO>> listResources(
            @PathVariable Long courseId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(resourceService.listResources(courseId, pageable));
    }

    @Operation(summary = "Get a resource by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{resourceId}")
    public ApiResponse<CourseResourceVO> getResource(@PathVariable Long resourceId) {
        return ApiResponse.success(resourceService.getResource(resourceId));
    }

    @Operation(summary = "Delete a resource", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{resourceId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'TA')")
    public ApiResponse<Void> deleteResource(@PathVariable Long resourceId) {
        resourceService.deleteResource(resourceId);
        return ApiResponse.success(null);
    }
}
