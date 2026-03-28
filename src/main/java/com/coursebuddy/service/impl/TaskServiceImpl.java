package com.coursebuddy.service.impl;

import com.coursebuddy.auth.User;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.domain.dto.CollaborationTaskDTO;
import com.coursebuddy.domain.po.CollaborationTaskPO;
import com.coursebuddy.domain.vo.CollaborationTaskVO;
import com.coursebuddy.mapper.CollaborationTaskMapper;
import com.coursebuddy.repository.CollaborationProjectRepository;
import com.coursebuddy.repository.CollaborationTaskRepository;
import com.coursebuddy.repository.ProjectMemberRepository;
import com.coursebuddy.service.ITaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements ITaskService {

    private final CollaborationTaskRepository taskRepository;
    private final CollaborationProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final CollaborationTaskMapper taskMapper;

    @Override
    @Transactional
    public CollaborationTaskVO createTask(CollaborationTaskDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (!projectRepository.existsById(dto.getProjectId())) {
            throw new BusinessException(404, "Project not found");
        }
        CollaborationTaskPO po = taskMapper.dtoToPo(dto);
        po.setProjectId(dto.getProjectId());
        po.setCreatorId(currentUser.getId());
        if (po.getStatus() == null) po.setStatus("TODO");
        if (po.getPriority() == null) po.setPriority("MEDIUM");
        if (po.getProgress() == null) po.setProgress(0);
        return taskMapper.poToVo(taskRepository.save(po));
    }

    @Override
    @Transactional
    public CollaborationTaskVO updateTask(Long id, CollaborationTaskDTO dto) {
        CollaborationTaskPO po = getTaskPo(id);
        if (dto.getTitle() != null) po.setTitle(dto.getTitle());
        if (dto.getDescription() != null) po.setDescription(dto.getDescription());
        if (dto.getAssigneeId() != null) po.setAssigneeId(dto.getAssigneeId());
        if (dto.getStatus() != null) po.setStatus(dto.getStatus());
        if (dto.getPriority() != null) po.setPriority(dto.getPriority());
        if (dto.getDueDate() != null) po.setDueDate(dto.getDueDate());
        return taskMapper.poToVo(taskRepository.save(po));
    }

    @Override
    @Transactional
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new BusinessException(404, "Task not found");
        }
        taskRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public CollaborationTaskVO getTask(Long id) {
        return taskMapper.poToVo(getTaskPo(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CollaborationTaskVO> listTasksByProject(Long projectId, String status, Pageable pageable) {
        if (!projectRepository.existsById(projectId)) {
            throw new BusinessException(404, "Project not found");
        }
        if (status != null && !status.isEmpty()) {
            return taskMapper.poPageToVoPage(
                    taskRepository.findByProjectIdAndStatus(projectId, status, pageable));
        }
        return taskMapper.poPageToVoPage(taskRepository.findByProjectId(projectId, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CollaborationTaskVO> listMyTasks(String status, Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (status != null && !status.isEmpty()) {
            return taskMapper.poPageToVoPage(
                    taskRepository.findByAssigneeIdAndStatus(currentUser.getId(), status, pageable));
        }
        return taskMapper.poPageToVoPage(taskRepository.findByAssigneeId(currentUser.getId(), pageable));
    }

    @Override
    @Transactional
    public CollaborationTaskVO assignTask(Long taskId, Long userId) {
        CollaborationTaskPO po = getTaskPo(taskId);
        // Verify the user is a project member
        if (!memberRepository.existsByProjectIdAndUserId(po.getProjectId(), userId)) {
            throw new BusinessException(400, "Cannot assign task to a non-project member");
        }
        po.setAssigneeId(userId);
        return taskMapper.poToVo(taskRepository.save(po));
    }

    @Override
    @Transactional
    public CollaborationTaskVO updateTaskProgress(Long taskId, int progress) {
        if (progress < 0 || progress > 100) {
            throw new BusinessException(400, "Progress must be between 0 and 100");
        }
        CollaborationTaskPO po = getTaskPo(taskId);
        po.setProgress(progress);
        return taskMapper.poToVo(taskRepository.save(po));
    }

    @Override
    @Transactional
    public CollaborationTaskVO completeTask(Long taskId) {
        CollaborationTaskPO po = getTaskPo(taskId);
        po.setStatus("DONE");
        po.setProgress(100);
        po.setCompletedAt(LocalDateTime.now());
        return taskMapper.poToVo(taskRepository.save(po));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CollaborationTaskVO> getTasksByAssignee(Long userId, Pageable pageable) {
        return taskMapper.poPageToVoPage(taskRepository.findByAssigneeId(userId, pageable));
    }

    private CollaborationTaskPO getTaskPo(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Task not found"));
    }
}
