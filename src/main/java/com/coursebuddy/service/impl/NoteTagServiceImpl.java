package com.coursebuddy.service.impl;

import com.coursebuddy.auth.User;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.domain.dto.NoteTagDTO;
import com.coursebuddy.domain.po.NoteTagPO;
import com.coursebuddy.domain.vo.NoteTagVO;
import com.coursebuddy.converter.NoteTagConverter;
import com.coursebuddy.mapper.NoteTagMapper;
import com.coursebuddy.service.INoteTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteTagServiceImpl implements INoteTagService {

    private final NoteTagMapper noteTagRepository;
    private final NoteTagConverter noteTagMapper;

    @Override
    @Transactional
    public NoteTagVO create(NoteTagDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (noteTagRepository.findByUserIdAndName(currentUser.getId(), dto.getName()).isPresent()) {
            throw new BusinessException(409, "Tag with this name already exists");
        }
        NoteTagPO po = noteTagMapper.dtoToPo(dto);
        po.setUserId(currentUser.getId());
        noteTagRepository.insert(po);
        return noteTagMapper.poToVo(po);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteTagVO> listMy(String keyword) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (keyword != null && !keyword.isBlank()) {
            return noteTagMapper.poListToVoList(
                    noteTagRepository.findByUserIdAndNameContainingIgnoreCase(currentUser.getId(), keyword));
        }
        return noteTagMapper.poListToVoList(
                noteTagRepository.findByUserIdOrderByUseCountDesc(currentUser.getId()));
    }

    @Override
    @Transactional
    public NoteTagVO update(Long id, NoteTagDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        NoteTagPO po = noteTagRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new BusinessException(404, "Tag not found"));
        if (dto.getName() != null) po.setName(dto.getName());
        if (dto.getColor() != null) po.setColor(dto.getColor());
        noteTagRepository.updateById(po);
        return noteTagMapper.poToVo(po);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        NoteTagPO po = noteTagRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new BusinessException(404, "Tag not found"));
        noteTagRepository.deleteById(po.getId());
    }
}
