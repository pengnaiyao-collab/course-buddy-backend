package com.coursebuddy.aiassistant;

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

@Tag(name = "AI Assistant", description = "AI assistant and learning task endpoints")
@RestController
@RequestMapping("/ai-assistant")
@RequiredArgsConstructor
public class AiAssistantController {

    private final AiAssistantService service;

    @Operation(summary = "Ask a question to the AI assistant", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/questions")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<QuestionResponse> askQuestion(@Valid @RequestBody QuestionRequest request) {
        return ApiResponse.success("Question submitted successfully", service.askQuestion(request));
    }

    @Operation(summary = "List my questions", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/questions")
    public ApiResponse<Page<QuestionResponse>> listMyQuestions(
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(service.listMyQuestions(pageable));
    }

    @Operation(summary = "Get a question by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/questions/{id}")
    public ApiResponse<QuestionResponse> getQuestion(@PathVariable Long id) {
        return ApiResponse.success(service.getQuestion(id));
    }

    @Operation(summary = "Get answers for a question", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/questions/{questionId}/answers")
    public ApiResponse<Page<AnswerResponse>> getAnswers(
            @PathVariable Long questionId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(service.getAnswers(questionId, pageable));
    }

    @Operation(summary = "Create a learning task", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/learning-tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<LearningTaskResponse> createTask(@Valid @RequestBody LearningTaskRequest request) {
        return ApiResponse.success("Learning task created successfully", service.createTask(request));
    }

    @Operation(summary = "List my learning tasks", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/learning-tasks")
    public ApiResponse<Page<LearningTaskResponse>> listMyTasks(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(service.listMyTasks(status, pageable));
    }

    @Operation(summary = "Get a learning task by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/learning-tasks/{id}")
    public ApiResponse<LearningTaskResponse> getTask(@PathVariable Long id) {
        return ApiResponse.success(service.getTask(id));
    }

    @Operation(summary = "Update a learning task", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/learning-tasks/{id}")
    public ApiResponse<LearningTaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody LearningTaskRequest request) {
        return ApiResponse.success(service.updateTask(id, request));
    }

    @Operation(summary = "Delete a learning task", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/learning-tasks/{id}")
    public ApiResponse<Void> deleteTask(@PathVariable Long id) {
        service.deleteTask(id);
        return ApiResponse.success(null);
    }
}
