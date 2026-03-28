package com.coursebuddy.service.impl;

import com.coursebuddy.auth.User;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.domain.dto.NoteShareDTO;
import com.coursebuddy.domain.po.NoteSharePO;
import com.coursebuddy.domain.vo.NoteShareVO;
import com.coursebuddy.mapper.NoteShareMapper;
import com.coursebuddy.repository.NoteShareRepository;
import com.coursebuddy.service.INoteShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NoteShareServiceImpl implements INoteShareService {

    private final NoteShareRepository noteShareRepository;
    private final NoteShareMapper noteShareMapper;

    @Override
    @Transactional
    public NoteShareVO createShare(Long noteId, NoteShareDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        NoteSharePO po = NoteSharePO.builder()
                .noteId(noteId)
                .ownerId(currentUser.getId())
                .shareToken(UUID.randomUUID().toString().replace("-", ""))
                .permission(dto.getPermission() != null ? dto.getPermission() : "READ")
                .expiresAt(dto.getExpiresAt())
                .build();
        return noteShareMapper.poToVo(noteShareRepository.save(po));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoteShareVO> listMyShares(Long noteId, Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (noteId != null) {
            return noteShareMapper.poPageToVoPage(
                    noteShareRepository.findByNoteIdAndOwnerIdOrderByCreatedAtDesc(noteId, currentUser.getId(), pageable));
        }
        return noteShareMapper.poPageToVoPage(
                noteShareRepository.findByOwnerIdOrderByCreatedAtDesc(currentUser.getId(), pageable));
    }

    @Override
    @Transactional
    public NoteShareVO getByToken(String shareToken) {
        NoteSharePO po = noteShareRepository.findByShareToken(shareToken)
                .orElseThrow(() -> new BusinessException(404, "Share link not found or expired"));
        if (!po.getIsActive()) {
            throw new BusinessException(410, "Share link is no longer active");
        }
        if (po.getExpiresAt() != null && po.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(410, "Share link has expired");
        }
        noteShareRepository.incrementAccessCount(po.getId());
        po.setAccessCount(po.getAccessCount() + 1);
        return noteShareMapper.poToVo(po);
    }

    @Override
    @Transactional
    public NoteShareVO updateShare(Long shareId, NoteShareDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        NoteSharePO po = noteShareRepository.findById(shareId)
                .orElseThrow(() -> new BusinessException(404, "Share not found"));
        if (!po.getOwnerId().equals(currentUser.getId())) {
            throw new BusinessException(403, "Access denied");
        }
        if (dto.getPermission() != null) po.setPermission(dto.getPermission());
        if (dto.getExpiresAt() != null) po.setExpiresAt(dto.getExpiresAt());
        return noteShareMapper.poToVo(noteShareRepository.save(po));
    }

    @Override
    @Transactional
    public void deleteShare(Long shareId) {
        User currentUser = SecurityUtils.getCurrentUser();
        NoteSharePO po = noteShareRepository.findById(shareId)
                .orElseThrow(() -> new BusinessException(404, "Share not found"));
        if (!po.getOwnerId().equals(currentUser.getId())) {
            throw new BusinessException(403, "Access denied");
        }
        noteShareRepository.delete(po);
    }
}
