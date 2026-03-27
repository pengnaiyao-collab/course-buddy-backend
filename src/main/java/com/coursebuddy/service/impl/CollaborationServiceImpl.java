package com.coursebuddy.service.impl;

import com.coursebuddy.auth.User;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.domain.dto.CollaborationProjectDTO;
import com.coursebuddy.domain.dto.CollaborationTaskDTO;
import com.coursebuddy.domain.po.CollaborationProjectPO;
import com.coursebuddy.domain.po.CollaborationTaskPO;
import com.coursebuddy.domain.vo.CollaborationProjectVO;
import com.coursebuddy.domain.vo.CollaborationTaskVO;
import com.coursebuddy.mapper.CollaborationProjectMapper;
import com.coursebuddy.mapper.CollaborationTaskMapper;
import com.coursebuddy.repository.CollaborationProjectRepository;
import com.coursebuddy.repository.CollaborationTaskRepository;
import com.coursebuddy.service.ICollaborationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CollaborationServiceImpl implements ICollaborationService {

    private final CollaborationProjectRepository projectRepository;
    private final CollaborationTaskRepository taskRepository;
    private final CollaborationProjectMapper projectMapper;
    private final CollaborationTaskMapper taskMapper;

    @Override
    @Transactional
    public CollaborationProjectVO createProject(CollaborationProjectDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        CollaborationProjectPO po = projectMapper.dtoToPo(dto);
        po.setOwnerId(currentUser.getId());
        if (po.getStatus() == null) po.setStatus("ACTIVE");
        return projectMapper.poToVo(projectRepository.save(po));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CollaborationProjectVO> listProjects(Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser();
        return projectMapper.poPageToVoPage(
                projectRepository.findByOwnerId(currentUser.getId(), pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public CollaborationProjectVO getProject(Long id) {
        CollaborationProjectPO po = projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Project not found"));
        return projectMapper.poToVo(po);
    }

    @Override
    @Transactional
    public CollaborationProjectVO updateProject(Long id, CollaborationProjectDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        CollaborationProjectPO po = projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Project not found"));
        if (!po.getOwnerId().equals(currentUser.getId())) {
            throw new BusinessException(403, "You are not authorized to update this project");
        }
        po.setName(dto.getName());
        po.setDescription(dto.getDescription());
        if (dto.getStatus() != null) po.setStatus(dto.getStatus());
        return projectMapper.poToVo(projectRepository.save(po));
    }

    @Override
    @Transactional
    public void deleteProject(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        CollaborationProjectPO po = projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Project not found"));
        if (!po.getOwnerId().equals(currentUser.getId())) {
            throw new BusinessException(403, "You are not authorized to delete this project");
        }
        projectRepository.delete(po);
    }

    @Override
    @Transactional
    public CollaborationTaskVO createTask(Long projectId, CollaborationTaskDTO dto) {
        if (!projectRepository.existsById(projectId)) {
            throw new BusinessException(404, "Project not found");
        }
        CollaborationTaskPO po = taskMapper.dtoToPo(dto);
        po.setProjectId(projectId);
        if (po.getStatus() == null) po.setStatus("TODO");
        if (po.getPriority() == null) po.setPriority("MEDIUM");
        return taskMapper.poToVo(taskRepository.save(po));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CollaborationTaskVO> listTasksByProject(Long projectId, Pageable pageable) {
        if (!projectRepository.existsById(projectId)) {
            throw new BusinessException(404, "Project not found");
        }
        return taskMapper.poPageToVoPage(taskRepository.findByProjectId(projectId, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public CollaborationTaskVO getTask(Long id) {
        CollaborationTaskPO po = taskRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Task not found"));
        return taskMapper.poToVo(po);
    }

    @Override
    @Transactional
    public CollaborationTaskVO updateTask(Long id, CollaborationTaskDTO dto) {
        CollaborationTaskPO po = taskRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Task not found"));
        po.setTitle(dto.getTitle());
        po.setDescription(dto.getDescription());
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
}
