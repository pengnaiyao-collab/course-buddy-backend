package com.coursebuddy.service.impl;

import com.coursebuddy.auth.User;
import com.coursebuddy.common.MybatisPlusPageUtils;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.coursebuddy.domain.dto.CollaborationProjectDTO;
import com.coursebuddy.domain.dto.CollaborationTaskDTO;
import com.coursebuddy.domain.po.CollaborationProjectPO;
import com.coursebuddy.domain.po.CollaborationTaskPO;
import com.coursebuddy.domain.vo.CollaborationProjectVO;
import com.coursebuddy.domain.vo.CollaborationTaskVO;
import com.coursebuddy.converter.CollaborationProjectConverter;
import com.coursebuddy.converter.CollaborationTaskConverter;
import com.coursebuddy.mapper.CollaborationProjectMapper;
import com.coursebuddy.mapper.CollaborationTaskMapper;
import com.coursebuddy.service.ICollaborationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CollaborationServiceImpl implements ICollaborationService {

    private final CollaborationProjectMapper projectRepository;
    private final CollaborationTaskMapper taskRepository;
    private final CollaborationProjectConverter projectMapper;
    private final CollaborationTaskConverter taskMapper;

    @Override
    @Transactional
    public CollaborationProjectVO createProject(CollaborationProjectDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        CollaborationProjectPO po = projectMapper.dtoToPo(dto);
        po.setOwnerId(currentUser.getId());
        if (po.getStatus() == null) po.setStatus("ACTIVE");
        projectRepository.insert(po);
        return projectMapper.poToVo(po);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CollaborationProjectVO> listProjects(Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser();
        IPage<CollaborationProjectPO> poPage =
                projectRepository.findByOwnerId(MybatisPlusPageUtils.toMpPage(pageable), currentUser.getId());
        return projectMapper.poPageToVoPage(
                MybatisPlusPageUtils.toSpringPage(poPage, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public CollaborationProjectVO getProject(Long id) {
        CollaborationProjectPO po = projectRepository.selectById(id);
        if (po == null) {
            throw new BusinessException(404, "Project not found");
        }
        return projectMapper.poToVo(po);
    }

    @Override
    @Transactional
    public CollaborationProjectVO updateProject(Long id, CollaborationProjectDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        CollaborationProjectPO po = projectRepository.selectById(id);
        if (po == null) {
            throw new BusinessException(404, "Project not found");
        }
        if (!po.getOwnerId().equals(currentUser.getId())) {
            throw new BusinessException(403, "You are not authorized to update this project");
        }
        po.setName(dto.getName());
        po.setDescription(dto.getDescription());
        if (dto.getStatus() != null) po.setStatus(dto.getStatus());
        projectRepository.updateById(po);
        return projectMapper.poToVo(po);
    }

    @Override
    @Transactional
    public void deleteProject(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        CollaborationProjectPO po = projectRepository.selectById(id);
        if (po == null) {
            throw new BusinessException(404, "Project not found");
        }
        if (!po.getOwnerId().equals(currentUser.getId())) {
            throw new BusinessException(403, "You are not authorized to delete this project");
        }
        projectRepository.deleteById(po.getId());
    }

    @Override
    @Transactional
    public CollaborationTaskVO createTask(Long projectId, CollaborationTaskDTO dto) {
        if (projectRepository.selectById(projectId) == null) {
            throw new BusinessException(404, "Project not found");
        }
        CollaborationTaskPO po = taskMapper.dtoToPo(dto);
        po.setProjectId(projectId);
        if (po.getStatus() == null) po.setStatus("TODO");
        if (po.getPriority() == null) po.setPriority("MEDIUM");
        taskRepository.insert(po);
        return taskMapper.poToVo(po);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CollaborationTaskVO> listTasksByProject(Long projectId, Pageable pageable) {
        if (projectRepository.selectById(projectId) == null) {
            throw new BusinessException(404, "Project not found");
        }
        IPage<CollaborationTaskPO> poPage =
                taskRepository.findByProjectId(MybatisPlusPageUtils.toMpPage(pageable), projectId);
        return taskMapper.poPageToVoPage(MybatisPlusPageUtils.toSpringPage(poPage, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public CollaborationTaskVO getTask(Long id) {
        CollaborationTaskPO po = taskRepository.selectById(id);
        if (po == null) {
            throw new BusinessException(404, "Task not found");
        }
        return taskMapper.poToVo(po);
    }

    @Override
    @Transactional
    public CollaborationTaskVO updateTask(Long id, CollaborationTaskDTO dto) {
        CollaborationTaskPO po = taskRepository.selectById(id);
        if (po == null) {
            throw new BusinessException(404, "Task not found");
        }
        po.setTitle(dto.getTitle());
        po.setDescription(dto.getDescription());
        if (dto.getAssigneeId() != null) po.setAssigneeId(dto.getAssigneeId());
        if (dto.getStatus() != null) po.setStatus(dto.getStatus());
        if (dto.getPriority() != null) po.setPriority(dto.getPriority());
        if (dto.getDueDate() != null) po.setDueDate(dto.getDueDate());
        taskRepository.updateById(po);
        return taskMapper.poToVo(po);
    }

    @Override
    @Transactional
    public void deleteTask(Long id) {
        if (taskRepository.selectById(id) == null) {
            throw new BusinessException(404, "Task not found");
        }
        taskRepository.deleteById(id);
    }
}
