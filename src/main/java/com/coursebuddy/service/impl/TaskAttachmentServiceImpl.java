package com.coursebuddy.service.impl;

import com.coursebuddy.auth.User;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.domain.dto.InitUploadRequest;
import com.coursebuddy.domain.po.TaskAttachmentPO;
import com.coursebuddy.domain.vo.ChunkUploadResponse;
import com.coursebuddy.domain.vo.FileUploadResponse;
import com.coursebuddy.domain.vo.InitUploadResponse;
import com.coursebuddy.domain.vo.TaskAttachmentVO;
import com.coursebuddy.converter.TaskAttachmentConverter;
import com.coursebuddy.mapper.CollaborationTaskMapper;
import com.coursebuddy.mapper.TaskAttachmentMapper;
import com.coursebuddy.service.ITaskAttachmentService;
import com.coursebuddy.service.IMinIOUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskAttachmentServiceImpl implements ITaskAttachmentService {

    private final TaskAttachmentMapper attachmentRepository;
    private final CollaborationTaskMapper taskRepository;
    private final TaskAttachmentConverter attachmentMapper;
    private final IMinIOUploadService minIOUploadService;

    @Override
    @Transactional
    public TaskAttachmentVO uploadAttachment(Long taskId, MultipartFile file) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (taskRepository.selectById(taskId) == null) {
            throw new BusinessException(404, "Task not found");
        }
        // Use the chunked upload approach from IMinIOUploadService
        InitUploadRequest request = new InitUploadRequest();
        request.setFileName(file.getOriginalFilename());
        request.setFileSize(file.getSize());
        
        InitUploadResponse initResponse = minIOUploadService.initUpload(request);
        ChunkUploadResponse chunkResponse = minIOUploadService.uploadChunk(initResponse.getSessionId(), 0, file);
        FileUploadResponse uploadResponse = minIOUploadService.mergeChunks(initResponse.getSessionId(), 1);
        
        TaskAttachmentPO po = TaskAttachmentPO.builder()
                .taskId(taskId)
                .fileUrl(uploadResponse.getUploadUrl())
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .fileType(file.getContentType())
                .uploadedBy(currentUser.getId())
                .build();
        attachmentRepository.insert(po);
        return attachmentMapper.poToVo(po);
    }

    @Override
    @Transactional
    public void deleteAttachment(Long attachmentId) {
        User currentUser = SecurityUtils.getCurrentUser();
        TaskAttachmentPO po = attachmentRepository.selectById(attachmentId);
        if (po == null) {
            throw new BusinessException(404, "Attachment not found");
        }
        if (!po.getUploadedBy().equals(currentUser.getId())) {
            throw new BusinessException(403, "You can only delete your own attachments");
        }
        attachmentRepository.deleteById(po.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskAttachmentVO> getAttachments(Long taskId) {
        if (taskRepository.selectById(taskId) == null) {
            throw new BusinessException(404, "Task not found");
        }
        return attachmentMapper.poListToVoList(attachmentRepository.findByTaskId(taskId));
    }
}
