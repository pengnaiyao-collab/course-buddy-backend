package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coursebuddy.domain.po.NoteVersionPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

/**
 * 笔记版本映射器
 */
@Mapper
public interface NoteVersionMapper extends BaseMapper<NoteVersionPO> {

    @Select("SELECT * FROM note_versions WHERE note_id = #{noteId} ORDER BY version_no DESC")
    List<NoteVersionPO> findByNoteIdOrderByVersionNoDesc(@Param("noteId") Long noteId);

    @Select("SELECT * FROM note_versions WHERE note_id = #{noteId} AND version_no = #{versionNo}")
    Optional<NoteVersionPO> findByNoteIdAndVersionNo(@Param("noteId") Long noteId, @Param("versionNo") Integer versionNo);

    @Select("SELECT COALESCE(MAX(version_no), 0) FROM note_versions WHERE note_id = #{noteId}")
    Integer getMaxVersionNo(@Param("noteId") Long noteId);
}
