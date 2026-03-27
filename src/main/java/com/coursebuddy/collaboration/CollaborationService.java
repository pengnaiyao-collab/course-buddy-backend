package com.coursebuddy.collaboration;

import com.coursebuddy.auth.User;
import com.coursebuddy.common.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CollaborationService {

    private final CollaborationProjectRepository projectRepository;
    private final CollaborationTaskRepository taskRepository;

    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {
        User currentUser = getCurrentUser();
        CollaborationProject project = CollaborationProject.builder()
                .courseId(request.getCourseId())
                .name(request.getName())
                .description(request.getDescription())
                .ownerId(currentUser.getId())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .build();
        return ProjectResponse.from(projectRepository.save(project));
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> listProjects(Pageable pageable) {
        User currentUser = getCurrentUser();
        return projectRepository.findByOwnerId(currentUser.getId(), pageable).map(ProjectResponse::from);
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProject(Long id) {
        return ProjectResponse.from(projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Project not found")));
    }

    @Transactional
    public ProjectResponse updateProject(Long id, ProjectRequest request) {
        User currentUser = getCurrentUser();
        CollaborationProject project = projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Project not found"));
        if (!project.getOwnerId().equals(currentUser.getId())) {
            throw new BusinessException(403, "You are not authorized to update this project");
        }
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        if (request.getStatus() != null) project.setStatus(request.getStatus());
        return ProjectResponse.from(projectRepository.save(project));
    }

    @Transactional
    public void deleteProject(Long id) {
        User currentUser = getCurrentUser();
        CollaborationProject project = projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Project not found"));
        if (!project.getOwnerId().equals(currentUser.getId())) {
            throw new BusinessException(403, "You are not authorized to delete this project");
        }
        projectRepository.delete(project);
    }

    @Transactional
    public TaskResponse createTask(Long projectId, TaskRequest request) {
        if (!projectRepository.existsById(projectId)) {
            throw new BusinessException(404, "Project not found");
        }
        CollaborationTask task = CollaborationTask.builder()
                .projectId(projectId)
                .title(request.getTitle())
                .description(request.getDescription())
                .assigneeId(request.getAssigneeId())
                .status(request.getStatus() != null ? request.getStatus() : "TODO")
                .priority(request.getPriority() != null ? request.getPriority() : "MEDIUM")
                .dueDate(request.getDueDate())
                .build();
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> listTasksByProject(Long projectId, Pageable pageable) {
        if (!projectRepository.existsById(projectId)) {
            throw new BusinessException(404, "Project not found");
        }
        return taskRepository.findByProjectId(projectId, pageable).map(TaskResponse::from);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(Long id) {
        return TaskResponse.from(taskRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Task not found")));
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request) {
        CollaborationTask task = taskRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Task not found"));
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        if (request.getAssigneeId() != null) task.setAssigneeId(request.getAssigneeId());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        if (request.getDueDate() != null) task.setDueDate(request.getDueDate());
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new BusinessException(404, "Task not found");
        }
        taskRepository.deleteById(id);
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User user) {
            return user;
        }
        throw new BusinessException(401, "User not authenticated");
    }
}
