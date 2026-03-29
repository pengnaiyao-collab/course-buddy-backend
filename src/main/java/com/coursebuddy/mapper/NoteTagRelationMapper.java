package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coursebuddy.domain.po.NoteTagRelationPO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 笔记-标签关联 Repository。
 */
@Mapper
public interface NoteTagRelationMapper extends BaseMapper<NoteTagRelationPO> {

    /**
     * 查询某篇笔记关联的所有标签 ID 列表。
     *
     * @param noteId 笔记 ID
     * @return 标签 ID 列表
     */
    @Select("SELECT tag_id FROM note_tag_relations WHERE note_id = #{noteId}")
    List<Long> findTagIdsByNoteId(@Param("noteId") Long noteId);

    /**
     * 查询关联了指定标签的所有笔记 ID 列表。
     *
     * @param tagId 标签 ID
     * @return 笔记 ID 列表
     */
    @Select("SELECT note_id FROM note_tag_relations WHERE tag_id = #{tagId}")
    List<Long> findNoteIdsByTagId(@Param("tagId") Long tagId);

    /**
     * 删除某篇笔记下所有标签关联。
     *
     * @param noteId 笔记 ID
     */
    @Delete("DELETE FROM note_tag_relations WHERE note_id = #{noteId}")
    void deleteByNoteId(@Param("noteId") Long noteId);

    /**
     * 删除某篇笔记与指定标签的单条关联。
     *
     * @param noteId 笔记 ID
     * @param tagId  标签 ID
     */
    @Delete("DELETE FROM note_tag_relations WHERE note_id = #{noteId} AND tag_id = #{tagId}")
    void deleteByNoteIdAndTagId(@Param("noteId") Long noteId, @Param("tagId") Long tagId);

    /**
     * 检查关联是否已存在。
     *
     * @param noteId 笔记 ID
     * @param tagId  标签 ID
     * @return true 若关联存在
     */
    @Select("SELECT COUNT(*) > 0 FROM note_tag_relations WHERE note_id = #{noteId} AND tag_id = #{tagId}")
    boolean existsByNoteIdAndTagId(@Param("noteId") Long noteId, @Param("tagId") Long tagId);

    /**
     * 查询包含全部指定标签的笔记 ID（逐标签 AND 过滤由业务层处理）。
     *
     * @param tagIds 标签 ID 列表
     * @return 笔记 ID 列表（含重复，用于 HAVING COUNT 场景）
     */
    @Select("<script>SELECT note_id FROM note_tag_relations WHERE tag_id IN <foreach collection='tagIds' item='tagId' open='(' separator=',' close=')'>#{tagId}</foreach> GROUP BY note_id HAVING COUNT(DISTINCT tag_id) = #{tagCount}</script>")
    List<Long> findNoteIdsHavingAllTags(@Param("tagIds") List<Long> tagIds, @Param("tagCount") long tagCount);
}
