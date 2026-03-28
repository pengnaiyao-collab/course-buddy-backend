package com.coursebuddy.service.impl;

import com.coursebuddy.auth.User;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.domain.po.NotePO;
import com.coursebuddy.domain.po.NoteVersionPO;
import com.coursebuddy.domain.vo.NoteVersionVO;
import com.coursebuddy.mapper.NoteVersionMapper;
import com.coursebuddy.repository.NoteRepository;
import com.coursebuddy.repository.NoteVersionRepository;
import com.coursebuddy.service.INoteVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteVersionServiceImpl implements INoteVersionService {

    private final NoteVersionRepository noteVersionRepository;
    private final NoteRepository noteRepository;
    private final NoteVersionMapper noteVersionMapper;

    @Override
    @Transactional
    public NoteVersionVO saveVersion(Long noteId, String changeDesc) {
        User currentUser = SecurityUtils.getCurrentUser();
        NotePO note = noteRepository.findById(noteId)
                .orElseThrow(() -> new BusinessException(404, "Note not found"));
        if (!note.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(403, "Access denied");
        }
        Integer nextVersion = noteVersionRepository.getMaxVersionNo(noteId) + 1;
        NoteVersionPO version = NoteVersionPO.builder()
                .noteId(noteId)
                .versionNo(nextVersion)
                .title(note.getTitle())
                .content(note.getContent())
                .changedBy(currentUser.getId())
                .changeDesc(changeDesc)
                .build();
        return noteVersionMapper.poToVo(noteVersionRepository.save(version));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteVersionVO> listVersions(Long noteId) {
        User currentUser = SecurityUtils.getCurrentUser();
        NotePO note = noteRepository.findById(noteId)
                .orElseThrow(() -> new BusinessException(404, "Note not found"));
        if (!note.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(403, "Access denied");
        }
        return noteVersionMapper.poListToVoList(
                noteVersionRepository.findByNoteIdOrderByVersionNoDesc(noteId));
    }

    @Override
    @Transactional(readOnly = true)
    public NoteVersionVO getVersion(Long noteId, Integer versionNo) {
        NoteVersionPO po = noteVersionRepository.findByNoteIdAndVersionNo(noteId, versionNo)
                .orElseThrow(() -> new BusinessException(404, "Version not found"));
        return noteVersionMapper.poToVo(po);
    }

    @Override
    @Transactional
    public void restoreVersion(Long noteId, Integer versionNo) {
        User currentUser = SecurityUtils.getCurrentUser();
        NotePO note = noteRepository.findById(noteId)
                .orElseThrow(() -> new BusinessException(404, "Note not found"));
        if (!note.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(403, "Access denied");
        }
        NoteVersionPO version = noteVersionRepository.findByNoteIdAndVersionNo(noteId, versionNo)
                .orElseThrow(() -> new BusinessException(404, "Version not found"));
        note.setTitle(version.getTitle());
        note.setContent(version.getContent());
        noteRepository.save(note);
    }
}
