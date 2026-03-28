package com.coursebuddy.service;

import com.coursebuddy.domain.dto.CollaborationProjectDTO;
import com.coursebuddy.domain.dto.ProjectMemberDTO;
import com.coursebuddy.domain.vo.CollaborationProjectVO;
import com.coursebuddy.domain.vo.ProjectMemberVO;
import com.coursebuddy.domain.vo.ProjectStatsVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IProjectService {
    CollaborationProjectVO createProject(CollaborationProjectDTO dto);
    CollaborationProjectVO updateProject(Long id, CollaborationProjectDTO dto);
    void deleteProject(Long id);
    CollaborationProjectVO getProject(Long id);
    Page<CollaborationProjectVO> listMyProjects(Pageable pageable);
    CollaborationProjectVO archiveProject(Long id);
    ProjectMemberVO addMember(Long projectId, ProjectMemberDTO dto);
    void removeMember(Long projectId, Long userId);
    ProjectMemberVO updateMemberRole(Long projectId, Long userId, String role);
    Page<ProjectMemberVO> listMembers(Long projectId, Pageable pageable);
    List<ProjectMemberVO> getMembers(Long projectId);
    ProjectStatsVO getProjectStats(Long projectId);
}
