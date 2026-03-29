package com.coursebuddy.service;

import com.coursebuddy.domain.vo.TaskAttachmentVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ITaskAttachmentService {
    TaskAttachmentVO uploadAttachment(Long taskId, MultipartFile file);
    void deleteAttachment(Long attachmentId);
    List<TaskAttachmentVO> getAttachments(Long taskId);
}
