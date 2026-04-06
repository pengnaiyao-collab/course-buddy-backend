package com.coursebuddy.converter;

import com.coursebuddy.domain.dto.NoteDTO;
import com.coursebuddy.domain.po.NotePO;
import com.coursebuddy.domain.vo.NoteVO;
import com.coursebuddy.common.exception.BusinessException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 笔记转换器
 */
@Component
public class NoteConverter {

    public NotePO dtoToPo(NoteDTO dto) {
        if (dto == null) return null;
        
        // 校验必填字段
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new BusinessException(400, "Note title cannot be empty");
        }
        if (dto.getTitle().length() > 255) {
            throw new BusinessException(400, "Note title cannot exceed 255 characters");
        }
        if (dto.getContent() != null && dto.getContent().length() > 1000000) {
            throw new BusinessException(400, "Note content is too large (max 1MB)");
        }
        
        return NotePO.builder()
                .courseId(dto.getCourseId())
                .categoryId(dto.getCategoryId())
                .title(dto.getTitle().trim())
                .content(dto.getContent() != null ? dto.getContent() : "")
                .category(dto.getCategory())
            .attachments(dto.getAttachments())
                .isPublic(dto.getIsPublic() != null ? dto.getIsPublic() : false)
                .build();
    }

    public NoteVO poToVo(NotePO po) {
        if (po == null) return null;
        return NoteVO.builder()
                .id(po.getId())
                .userId(po.getUserId())
                .courseId(po.getCourseId())
                .categoryId(po.getCategoryId())
                .title(po.getTitle())
                .content(po.getContent())
                .category(po.getCategory())
                .attachments(po.getAttachments())
                .isPublic(po.getIsPublic())
                .version(po.getOptLockVersion())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    public List<NoteVO> poListToVoList(List<NotePO> list) {
        if (list == null) return null;
        return list.stream().map(this::poToVo).collect(Collectors.toList());
    }

    public Page<NoteVO> poPageToVoPage(Page<NotePO> page) {
        return page.map(this::poToVo);
    }
}
