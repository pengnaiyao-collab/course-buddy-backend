package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coursebuddy.domain.po.NoteSharePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Optional;

@Mapper
public interface NoteShareMapper extends BaseMapper<NoteSharePO> {

    @Select("SELECT * FROM note_shares WHERE share_token = #{shareToken}")
    Optional<NoteSharePO> findByShareToken(@Param("shareToken") String shareToken);

    @Select("SELECT * FROM note_shares WHERE owner_id = #{ownerId} ORDER BY created_at DESC")
    IPage<NoteSharePO> findByOwnerIdOrderByCreatedAtDesc(Page<NoteSharePO> page, @Param("ownerId") Long ownerId);

    @Select("SELECT * FROM note_shares WHERE note_id = #{noteId} AND owner_id = #{ownerId} ORDER BY created_at DESC")
    IPage<NoteSharePO> findByNoteIdAndOwnerIdOrderByCreatedAtDesc(Page<NoteSharePO> page, @Param("noteId") Long noteId, @Param("ownerId") Long ownerId);

    @Update("UPDATE note_shares SET access_count = access_count + 1 WHERE id = #{id}")
    int incrementAccessCount(@Param("id") Long id);
}
