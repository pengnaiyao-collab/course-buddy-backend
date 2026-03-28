package com.coursebuddy.service;

import com.coursebuddy.domain.vo.NoteVersionVO;

import java.util.List;

public interface INoteVersionService {
    NoteVersionVO saveVersion(Long noteId, String changeDesc);
    List<NoteVersionVO> listVersions(Long noteId);
    NoteVersionVO getVersion(Long noteId, Integer versionNo);
    void restoreVersion(Long noteId, Integer versionNo);
}
