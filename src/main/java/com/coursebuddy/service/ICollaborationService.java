package com.coursebuddy.service;

import com.coursebuddy.domain.dto.CollaborationProjectDTO;
import com.coursebuddy.domain.dto.CollaborationTaskDTO;
import com.coursebuddy.domain.vo.CollaborationProjectVO;
import com.coursebuddy.domain.vo.CollaborationTaskVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ICollaborationService {
    CollaborationProjectVO createProject(CollaborationProjectDTO dto);
    Page<CollaborationProjectVO> listProjects(Pageable pageable);
    CollaborationProjectVO getProject(Long id);
    CollaborationProjectVO updateProject(Long id, CollaborationProjectDTO dto);
    void deleteProject(Long id);
    CollaborationTaskVO createTask(Long projectId, CollaborationTaskDTO dto);
    Page<CollaborationTaskVO> listTasksByProject(Long projectId, Pageable pageable);
    CollaborationTaskVO getTask(Long id);
    CollaborationTaskVO updateTask(Long id, CollaborationTaskDTO dto);
    void deleteTask(Long id);
}
