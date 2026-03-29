package com.coursebuddy.converter;

import com.coursebuddy.domain.po.TaskCommentPO;
import com.coursebuddy.domain.vo.TaskCommentVO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TaskCommentConverter {

    public TaskCommentVO poToVo(TaskCommentPO po) {
        if (po == null) return null;
        return TaskCommentVO.builder()
                .id(po.getId())
                .taskId(po.getTaskId())
                .userId(po.getUserId())
                .content(po.getContent())
                .attachmentUrl(po.getAttachmentUrl())
                .isEdited(po.getIsEdited())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    public List<TaskCommentVO> poListToVoList(List<TaskCommentPO> list) {
        if (list == null) return null;
        return list.stream().map(this::poToVo).collect(Collectors.toList());
    }

    public Page<TaskCommentVO> poPageToVoPage(Page<TaskCommentPO> page) {
        return page.map(this::poToVo);
    }
}
