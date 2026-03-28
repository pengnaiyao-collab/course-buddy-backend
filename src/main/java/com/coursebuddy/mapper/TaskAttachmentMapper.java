package com.coursebuddy.mapper;

import com.coursebuddy.domain.po.TaskAttachmentPO;
import com.coursebuddy.domain.vo.TaskAttachmentVO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TaskAttachmentMapper {

    public TaskAttachmentVO poToVo(TaskAttachmentPO po) {
        if (po == null) return null;
        return TaskAttachmentVO.builder()
                .id(po.getId())
                .taskId(po.getTaskId())
                .fileUrl(po.getFileUrl())
                .fileName(po.getFileName())
                .fileSize(po.getFileSize())
                .fileType(po.getFileType())
                .uploadedBy(po.getUploadedBy())
                .createdAt(po.getCreatedAt())
                .build();
    }

    public List<TaskAttachmentVO> poListToVoList(List<TaskAttachmentPO> list) {
        if (list == null) return null;
        return list.stream().map(this::poToVo).collect(Collectors.toList());
    }
}
