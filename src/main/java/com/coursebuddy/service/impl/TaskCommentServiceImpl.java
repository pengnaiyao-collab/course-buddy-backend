package com.coursebuddy.service.impl;

import com.coursebuddy.auth.User;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.domain.dto.TaskCommentDTO;
import com.coursebuddy.domain.po.TaskCommentPO;
import com.coursebuddy.domain.vo.TaskCommentVO;
import com.coursebuddy.mapper.TaskCommentMapper;
import com.coursebuddy.repository.CollaborationTaskRepository;
import com.coursebuddy.repository.TaskCommentRepository;
import com.coursebuddy.service.ITaskCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TaskCommentServiceImpl implements ITaskCommentService {

    private final TaskCommentRepository commentRepository;
    private final CollaborationTaskRepository taskRepository;
    private final TaskCommentMapper commentMapper;

    @Override
    @Transactional
    public TaskCommentVO addComment(Long taskId, TaskCommentDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (!taskRepository.existsById(taskId)) {
            throw new BusinessException(404, "Task not found");
        }
        TaskCommentPO po = TaskCommentPO.builder()
                .taskId(taskId)
                .userId(currentUser.getId())
                .content(dto.getContent())
                .attachmentUrl(dto.getAttachmentUrl())
                .isEdited(false)
                .build();
        return commentMapper.poToVo(commentRepository.save(po));
    }

    @Override
    @Transactional
    public TaskCommentVO updateComment(Long commentId, TaskCommentDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        TaskCommentPO po = getCommentPo(commentId);
        if (!po.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(403, "You can only edit your own comments");
        }
        po.setContent(dto.getContent());
        if (dto.getAttachmentUrl() != null) po.setAttachmentUrl(dto.getAttachmentUrl());
        po.setIsEdited(true);
        return commentMapper.poToVo(commentRepository.save(po));
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        User currentUser = SecurityUtils.getCurrentUser();
        TaskCommentPO po = getCommentPo(commentId);
        if (!po.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(403, "You can only delete your own comments");
        }
        po.setDeletedAt(LocalDateTime.now());
        commentRepository.save(po);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskCommentVO> getComments(Long taskId, Pageable pageable) {
        if (!taskRepository.existsById(taskId)) {
            throw new BusinessException(404, "Task not found");
        }
        return commentMapper.poPageToVoPage(
                commentRepository.findByTaskIdAndDeletedAtIsNull(taskId, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public TaskCommentVO getComment(Long commentId) {
        return commentMapper.poToVo(getCommentPo(commentId));
    }

    private TaskCommentPO getCommentPo(Long commentId) {
        TaskCommentPO po = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(404, "Comment not found"));
        if (po.getDeletedAt() != null) {
            throw new BusinessException(404, "Comment not found");
        }
        return po;
    }
}
