package com.coursebuddy.service;

import com.coursebuddy.domain.dto.CollaborationTaskDTO;
import com.coursebuddy.domain.vo.CollaborationTaskVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ITaskService {
    CollaborationTaskVO createTask(CollaborationTaskDTO dto);
    CollaborationTaskVO updateTask(Long id, CollaborationTaskDTO dto);
    void deleteTask(Long id);
    CollaborationTaskVO getTask(Long id);
    Page<CollaborationTaskVO> listTasksByProject(Long projectId, String status, Pageable pageable);
    Page<CollaborationTaskVO> listMyTasks(String status, Pageable pageable);
    CollaborationTaskVO assignTask(Long taskId, Long userId);
    CollaborationTaskVO updateTaskProgress(Long taskId, int progress);
    CollaborationTaskVO completeTask(Long taskId);
    Page<CollaborationTaskVO> getTasksByAssignee(Long userId, Pageable pageable);
}
