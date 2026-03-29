package com.coursebuddy.service;

import com.coursebuddy.domain.dto.TaskCommentDTO;
import com.coursebuddy.domain.vo.TaskCommentVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ITaskCommentService {
    TaskCommentVO addComment(Long taskId, TaskCommentDTO dto);
    TaskCommentVO updateComment(Long commentId, TaskCommentDTO dto);
    void deleteComment(Long commentId);
    Page<TaskCommentVO> getComments(Long taskId, Pageable pageable);
    TaskCommentVO getComment(Long commentId);
}
