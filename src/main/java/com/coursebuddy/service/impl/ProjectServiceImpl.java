package com.coursebuddy.service.impl;

import com.coursebuddy.auth.User;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.domain.dto.CollaborationProjectDTO;
import com.coursebuddy.domain.dto.ProjectMemberDTO;
import com.coursebuddy.domain.po.CollaborationProjectPO;
import com.coursebuddy.domain.po.CollaborationTaskPO;
import com.coursebuddy.domain.po.ProjectMemberPO;
import com.coursebuddy.domain.vo.CollaborationProjectVO;
import com.coursebuddy.domain.vo.ProjectMemberVO;
import com.coursebuddy.domain.vo.ProjectStatsVO;
import com.coursebuddy.mapper.CollaborationProjectMapper;
import com.coursebuddy.mapper.ProjectMemberMapper;
import com.coursebuddy.repository.CollaborationProjectRepository;
import com.coursebuddy.repository.CollaborationTaskRepository;
import com.coursebuddy.repository.ProjectMemberRepository;
import com.coursebuddy.repository.TaskAttachmentRepository;
import com.coursebuddy.repository.TaskCommentRepository;
import com.coursebuddy.service.IProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements IProjectService {

    private final CollaborationProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final CollaborationTaskRepository taskRepository;
    private final TaskCommentRepository commentRepository;
    private final TaskAttachmentRepository attachmentRepository;
    private final CollaborationProjectMapper projectMapper;
    private final ProjectMemberMapper memberMapper;

    @Override
    @Transactional
    public CollaborationProjectVO createProject(CollaborationProjectDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        CollaborationProjectPO po = projectMapper.dtoToPo(dto);
        po.setOwnerId(currentUser.getId());
        if (po.getStatus() == null) po.setStatus("ACTIVE");
        CollaborationProjectPO saved = projectRepository.save(po);
        // Auto-add owner as OWNER member
        ProjectMemberPO ownerMember = ProjectMemberPO.builder()
                .projectId(saved.getId())
                .userId(currentUser.getId())
                .role("OWNER")
                .build();
        memberRepository.save(ownerMember);
        return projectMapper.poToVo(saved);
    }

    @Override
    @Transactional
    public CollaborationProjectVO updateProject(Long id, CollaborationProjectDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        CollaborationProjectPO po = getProjectPo(id);
        checkOwnerOrManager(po.getId(), currentUser.getId());
        if (dto.getName() != null) po.setName(dto.getName());
        if (dto.getDescription() != null) po.setDescription(dto.getDescription());
        if (dto.getStatus() != null) po.setStatus(dto.getStatus());
        if (dto.getCoverUrl() != null) po.setCoverUrl(dto.getCoverUrl());
        if (dto.getIsPublic() != null) po.setIsPublic(dto.getIsPublic());
        return projectMapper.poToVo(projectRepository.save(po));
    }

    @Override
    @Transactional
    public void deleteProject(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        CollaborationProjectPO po = getProjectPo(id);
        if (!po.getOwnerId().equals(currentUser.getId())) {
            throw new BusinessException(403, "Only the project owner can delete the project");
        }
        projectRepository.delete(po);
    }

    @Override
    @Transactional(readOnly = true)
    public CollaborationProjectVO getProject(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        CollaborationProjectPO po = getProjectPo(id);
        checkMemberAccess(id, currentUser.getId(), po.getOwnerId());
        return projectMapper.poToVo(po);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CollaborationProjectVO> listMyProjects(Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser();
        return projectRepository.findByOwnerId(currentUser.getId(), pageable)
                .map(projectMapper::poToVo);
    }

    @Override
    @Transactional
    public CollaborationProjectVO archiveProject(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        CollaborationProjectPO po = getProjectPo(id);
        checkOwnerOrManager(id, currentUser.getId());
        po.setStatus("ARCHIVED");
        return projectMapper.poToVo(projectRepository.save(po));
    }

    @Override
    @Transactional
    public ProjectMemberVO addMember(Long projectId, ProjectMemberDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        getProjectPo(projectId);
        checkOwnerOrManager(projectId, currentUser.getId());
        if (memberRepository.existsByProjectIdAndUserId(projectId, dto.getUserId())) {
            throw new BusinessException(409, "User is already a member of this project");
        }
        ProjectMemberPO member = ProjectMemberPO.builder()
                .projectId(projectId)
                .userId(dto.getUserId())
                .role(dto.getRole() != null ? dto.getRole() : "MEMBER")
                .build();
        return memberMapper.poToVo(memberRepository.save(member));
    }

    @Override
    @Transactional
    public void removeMember(Long projectId, Long userId) {
        User currentUser = SecurityUtils.getCurrentUser();
        CollaborationProjectPO po = getProjectPo(projectId);
        if (!po.getOwnerId().equals(currentUser.getId()) && !currentUser.getId().equals(userId)) {
            throw new BusinessException(403, "Not authorized to remove this member");
        }
        memberRepository.deleteByProjectIdAndUserId(projectId, userId);
    }

    @Override
    @Transactional
    public ProjectMemberVO updateMemberRole(Long projectId, Long userId, String role) {
        User currentUser = SecurityUtils.getCurrentUser();
        getProjectPo(projectId);
        checkOwnerOrManager(projectId, currentUser.getId());
        ProjectMemberPO member = memberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new BusinessException(404, "Member not found"));
        member.setRole(role);
        return memberMapper.poToVo(memberRepository.save(member));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectMemberVO> listMembers(Long projectId, Pageable pageable) {
        getProjectPo(projectId);
        return memberMapper.poPageToVoPage(memberRepository.findByProjectId(projectId, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectMemberVO> getMembers(Long projectId) {
        getProjectPo(projectId);
        return memberMapper.poListToVoList(memberRepository.findByProjectId(projectId));
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectStatsVO getProjectStats(Long projectId) {
        CollaborationProjectPO po = getProjectPo(projectId);
        long total = taskRepository.countByProjectId(projectId);
        long todo = taskRepository.countByProjectIdAndStatus(projectId, "TODO");
        long inProgress = taskRepository.countByProjectIdAndStatus(projectId, "IN_PROGRESS");
        long review = taskRepository.countByProjectIdAndStatus(projectId, "REVIEW");
        long done = taskRepository.countByProjectIdAndStatus(projectId, "DONE");
        long members = memberRepository.countByProjectId(projectId);
        double completionRate = total > 0 ? (double) done / total * 100 : 0;
        return ProjectStatsVO.builder()
                .projectId(projectId)
                .projectName(po.getName())
                .totalTasks(total)
                .todoTasks(todo)
                .inProgressTasks(inProgress)
                .reviewTasks(review)
                .doneTasks(done)
                .totalMembers(members)
                .completionRate(completionRate)
                .build();
    }

    private CollaborationProjectPO getProjectPo(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Project not found"));
    }

    private void checkOwnerOrManager(Long projectId, Long userId) {
        ProjectMemberPO member = memberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new BusinessException(403, "Access denied: not a project member"));
        if (!"OWNER".equals(member.getRole()) && !"MANAGER".equals(member.getRole())) {
            throw new BusinessException(403, "Access denied: insufficient project role");
        }
    }

    private void checkMemberAccess(Long projectId, Long userId, Long ownerId) {
        if (!ownerId.equals(userId) && !memberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new BusinessException(403, "Access denied: not a project member");
        }
    }
}
