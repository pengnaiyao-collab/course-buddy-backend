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
    public CollaborationProject createProject(CollaborationProject project) {
        User currentUser = getCurrentUser();
        project.setOwnerId(currentUser.getId());
        if (project.getStatus() == null) project.setStatus("ACTIVE");
        return projectRepository.save(project);
    }

    @Transactional(readOnly = true)
    public Page<CollaborationProject> listProjects(Pageable pageable) {
        User currentUser = getCurrentUser();
        return projectRepository.findByOwnerId(currentUser.getId(), pageable);
    }

    @Transactional(readOnly = true)
    public CollaborationProject getProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Project not found"));
    }

    @Transactional
    public CollaborationProject updateProject(Long id, CollaborationProject item) {
        User currentUser = getCurrentUser();
        CollaborationProject project = projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Project not found"));
        if (!project.getOwnerId().equals(currentUser.getId())) {
            throw new BusinessException(403, "You are not authorized to update this project");
        }
        project.setName(item.getName());
        project.setDescription(item.getDescription());
        if (item.getStatus() != null) project.setStatus(item.getStatus());
        return projectRepository.save(project);
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
    public CollaborationTask createTask(Long projectId, CollaborationTask task) {
        if (!projectRepository.existsById(projectId)) {
            throw new BusinessException(404, "Project not found");
        }
        task.setProjectId(projectId);
        if (task.getStatus() == null) task.setStatus("TODO");
        if (task.getPriority() == null) task.setPriority("MEDIUM");
        return taskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public Page<CollaborationTask> listTasksByProject(Long projectId, Pageable pageable) {
        if (!projectRepository.existsById(projectId)) {
            throw new BusinessException(404, "Project not found");
        }
        return taskRepository.findByProjectId(projectId, pageable);
    }

    @Transactional(readOnly = true)
    public CollaborationTask getTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Task not found"));
    }

    @Transactional
    public CollaborationTask updateTask(Long id, CollaborationTask item) {
        CollaborationTask task = taskRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Task not found"));
        task.setTitle(item.getTitle());
        task.setDescription(item.getDescription());
        if (item.getAssigneeId() != null) task.setAssigneeId(item.getAssigneeId());
        if (item.getStatus() != null) task.setStatus(item.getStatus());
        if (item.getPriority() != null) task.setPriority(item.getPriority());
        if (item.getDueDate() != null) task.setDueDate(item.getDueDate());
        return taskRepository.save(task);
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
