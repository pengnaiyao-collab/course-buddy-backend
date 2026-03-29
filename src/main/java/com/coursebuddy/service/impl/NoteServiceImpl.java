package com.coursebuddy.service.impl;

import com.coursebuddy.auth.User;
import com.coursebuddy.common.MybatisPlusPageUtils;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.coursebuddy.domain.dto.NoteDTO;
import com.coursebuddy.domain.po.NotePO;
import com.coursebuddy.domain.vo.NoteVO;
import com.coursebuddy.converter.NoteConverter;
import com.coursebuddy.mapper.NoteMapper;
import com.coursebuddy.service.INoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements INoteService {

    private final NoteMapper noteRepository;
    private final NoteConverter noteMapper;

    @Override
    @Transactional
    public NoteVO create(NoteDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        NotePO po = noteMapper.dtoToPo(dto);
        po.setUserId(currentUser.getId());
        noteRepository.insert(po);
        return noteMapper.poToVo(po);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoteVO> listMyNotes(Long courseId, Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (courseId != null) {
            IPage<NotePO> poPage = noteRepository.findByUserIdAndCourseIdAndIsDeletedFalse(
                    MybatisPlusPageUtils.toMpPage(pageable), currentUser.getId(), courseId);
            return noteMapper.poPageToVoPage(
                    MybatisPlusPageUtils.toSpringPage(poPage, pageable));
        }
        IPage<NotePO> poPage = noteRepository.findByUserIdAndIsDeletedFalse(
                MybatisPlusPageUtils.toMpPage(pageable), currentUser.getId());
        return noteMapper.poPageToVoPage(MybatisPlusPageUtils.toSpringPage(poPage, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public NoteVO getById(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        NotePO po = noteRepository.selectById(id);
        if (po == null) {
            throw new BusinessException(404, "Note not found");
        }
        if (!po.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(403, "You are not authorized to view this note");
        }
        return noteMapper.poToVo(po);
    }

    @Override
    @Transactional
    public NoteVO update(Long id, NoteDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        NotePO po = noteRepository.selectById(id);
        if (po == null) {
            throw new BusinessException(404, "Note not found");
        }
        if (!po.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(403, "You are not authorized to update this note");
        }
        po.setTitle(dto.getTitle());
        po.setContent(dto.getContent());
        if (dto.getCategory() != null) po.setCategory(dto.getCategory());
        if (dto.getTags() != null) po.setTags(dto.getTags());
        noteRepository.updateById(po);
        return noteMapper.poToVo(po);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        NotePO po = noteRepository.selectById(id);
        if (po == null) {
            throw new BusinessException(404, "Note not found");
        }
        if (!po.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(403, "You are not authorized to delete this note");
        }
        noteRepository.deleteById(po.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoteVO> search(String keyword, Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser();
        IPage<NotePO> poPage = noteRepository.findByUserIdAndTitleContainingIgnoreCaseAndIsDeletedFalse(
                MybatisPlusPageUtils.toMpPage(pageable), currentUser.getId(), keyword);
        return noteMapper.poPageToVoPage(MybatisPlusPageUtils.toSpringPage(poPage, pageable));
    }
}
