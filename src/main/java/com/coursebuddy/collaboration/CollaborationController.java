package com.coursebuddy.collaboration;

import com.coursebuddy.common.ApiResponse;
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

@Tag(name = "Collaboration", description = "Collaboration project and task endpoints")
@RestController
@RequestMapping("/collaboration")
@RequiredArgsConstructor
public class CollaborationController {

    private final CollaborationService service;

    @Operation(summary = "Create a collaboration project", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/projects")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProjectResponse> createProject(@Valid @RequestBody ProjectRequest request) {
        return ApiResponse.success("Project created successfully", service.createProject(request));
    }

    @Operation(summary = "List my collaboration projects", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/projects")
    public ApiResponse<Page<ProjectResponse>> listProjects(
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(service.listProjects(pageable));
    }

    @Operation(summary = "Get a project by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/projects/{id}")
    public ApiResponse<ProjectResponse> getProject(@PathVariable Long id) {
        return ApiResponse.success(service.getProject(id));
    }

    @Operation(summary = "Update a project", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/projects/{id}")
    public ApiResponse<ProjectResponse> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequest request) {
        return ApiResponse.success(service.updateProject(id, request));
    }

    @Operation(summary = "Delete a project", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/projects/{id}")
    public ApiResponse<Void> deleteProject(@PathVariable Long id) {
        service.deleteProject(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "Create a task in a project", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/projects/{projectId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TaskResponse> createTask(
            @PathVariable Long projectId,
            @Valid @RequestBody TaskRequest request) {
        return ApiResponse.success("Task created successfully", service.createTask(projectId, request));
    }

    @Operation(summary = "List tasks in a project", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/projects/{projectId}/tasks")
    public ApiResponse<Page<TaskResponse>> listTasks(
            @PathVariable Long projectId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(service.listTasksByProject(projectId, pageable));
    }

    @Operation(summary = "Get a task by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/tasks/{id}")
    public ApiResponse<TaskResponse> getTask(@PathVariable Long id) {
        return ApiResponse.success(service.getTask(id));
    }

    @Operation(summary = "Update a task", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/tasks/{id}")
    public ApiResponse<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {
        return ApiResponse.success(service.updateTask(id, request));
    }

    @Operation(summary = "Delete a task", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/tasks/{id}")
    public ApiResponse<Void> deleteTask(@PathVariable Long id) {
        service.deleteTask(id);
        return ApiResponse.success(null);
    }
}
