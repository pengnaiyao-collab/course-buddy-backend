package com.coursebuddy.service.impl;

import com.coursebuddy.auth.User;
import com.coursebuddy.common.MybatisPlusPageUtils;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.coursebuddy.domain.dto.CollaborationTaskDTO;
import com.coursebuddy.domain.po.CollaborationTaskPO;
import com.coursebuddy.domain.vo.CollaborationTaskVO;
import com.coursebuddy.converter.CollaborationTaskConverter;
import com.coursebuddy.mapper.CollaborationProjectMapper;
import com.coursebuddy.mapper.CollaborationTaskMapper;
import com.coursebuddy.mapper.ProjectMemberMapper;
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

    private final CollaborationTaskMapper taskRepository;
    private final CollaborationProjectMapper projectRepository;
    private final ProjectMemberMapper memberRepository;
    private final CollaborationTaskConverter taskMapper;

    @Override
    @Transactional
    public CollaborationTaskVO createTask(CollaborationTaskDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (projectRepository.selectById(dto.getProjectId()) == null) {
            throw new BusinessException(404, "Project not found");
        }
        CollaborationTaskPO po = taskMapper.dtoToPo(dto);
        po.setProjectId(dto.getProjectId());
        po.setCreatorId(currentUser.getId());
        if (po.getStatus() == null) po.setStatus("TODO");
        if (po.getPriority() == null) po.setPriority("MEDIUM");
        if (po.getProgress() == null) po.setProgress(0);
        taskRepository.insert(po);
        return taskMapper.poToVo(po);
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

    @Override
    @Transactional(readOnly = true)
    public CollaborationTaskVO getTask(Long id) {
        return taskMapper.poToVo(getTaskPo(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CollaborationTaskVO> listTasksByProject(Long projectId, String status, Pageable pageable) {
        if (projectRepository.selectById(projectId) == null) {
            throw new BusinessException(404, "Project not found");
        }
        if (status != null && !status.isEmpty()) {
            IPage<CollaborationTaskPO> poPage = taskRepository.findByProjectIdAndStatus(
                    MybatisPlusPageUtils.toMpPage(pageable), projectId, status);
            return taskMapper.poPageToVoPage(
                    MybatisPlusPageUtils.toSpringPage(poPage, pageable));
        }
        IPage<CollaborationTaskPO> poPage = taskRepository.findByProjectId(
                MybatisPlusPageUtils.toMpPage(pageable), projectId);
        return taskMapper.poPageToVoPage(MybatisPlusPageUtils.toSpringPage(poPage, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CollaborationTaskVO> listMyTasks(String status, Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (status != null && !status.isEmpty()) {
            IPage<CollaborationTaskPO> poPage = taskRepository.findByAssigneeIdAndStatus(
                    MybatisPlusPageUtils.toMpPage(pageable), currentUser.getId(), status);
            return taskMapper.poPageToVoPage(
                    MybatisPlusPageUtils.toSpringPage(poPage, pageable));
        }
        IPage<CollaborationTaskPO> poPage = taskRepository.findByAssigneeId(
                MybatisPlusPageUtils.toMpPage(pageable), currentUser.getId());
        return taskMapper.poPageToVoPage(MybatisPlusPageUtils.toSpringPage(poPage, pageable));
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
        taskRepository.updateById(po);
        return taskMapper.poToVo(po);
    }

    @Override
    @Transactional
    public CollaborationTaskVO updateTaskProgress(Long taskId, int progress) {
        if (progress < 0 || progress > 100) {
            throw new BusinessException(400, "Progress must be between 0 and 100");
        }
        CollaborationTaskPO po = getTaskPo(taskId);
        po.setProgress(progress);
        taskRepository.updateById(po);
        return taskMapper.poToVo(po);
    }

    @Override
    @Transactional
    public CollaborationTaskVO completeTask(Long taskId) {
        CollaborationTaskPO po = getTaskPo(taskId);
        po.setStatus("DONE");
        po.setProgress(100);
        po.setCompletedAt(LocalDateTime.now());
        taskRepository.updateById(po);
        return taskMapper.poToVo(po);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CollaborationTaskVO> getTasksByAssignee(Long userId, Pageable pageable) {
        IPage<CollaborationTaskPO> poPage = taskRepository.findByAssigneeId(
                MybatisPlusPageUtils.toMpPage(pageable), userId);
        return taskMapper.poPageToVoPage(MybatisPlusPageUtils.toSpringPage(poPage, pageable));
    }

    private CollaborationTaskPO getTaskPo(Long id) {
        CollaborationTaskPO po = taskRepository.selectById(id);
        if (po == null) {
            throw new BusinessException(404, "Task not found");
        }
        return po;
    }
}
