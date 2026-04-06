package com.coursebuddy.service;

import com.coursebuddy.domain.vo.NoteVersionVO;

import java.util.List;

/**
 * 笔记版本服务
 */
public interface INoteVersionService {
    NoteVersionVO saveVersion(Long noteId, String changeDesc);
    List<NoteVersionVO> listVersions(Long noteId);
    NoteVersionVO getVersion(Long noteId, Integer versionNo);
    void restoreVersion(Long noteId, Integer versionNo);
}
