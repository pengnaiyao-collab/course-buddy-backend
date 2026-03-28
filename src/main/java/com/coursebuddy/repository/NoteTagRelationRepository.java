package com.coursebuddy.repository;

import com.coursebuddy.domain.po.NoteTagRelationPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 笔记-标签关联 Repository。
 */
@Repository
public interface NoteTagRelationRepository extends JpaRepository<NoteTagRelationPO, Long> {

    /**
     * 查询某篇笔记关联的所有标签 ID 列表。
     *
     * @param noteId 笔记 ID
     * @return 标签 ID 列表
     */
    @Query("SELECT r.tagId FROM NoteTagRelationPO r WHERE r.noteId = :noteId")
    List<Long> findTagIdsByNoteId(Long noteId);

    /**
     * 查询关联了指定标签的所有笔记 ID 列表。
     *
     * @param tagId 标签 ID
     * @return 笔记 ID 列表
     */
    @Query("SELECT r.noteId FROM NoteTagRelationPO r WHERE r.tagId = :tagId")
    List<Long> findNoteIdsByTagId(Long tagId);

    /**
     * 删除某篇笔记下所有标签关联。
     *
     * @param noteId 笔记 ID
     */
    @Modifying
    @Query("DELETE FROM NoteTagRelationPO r WHERE r.noteId = :noteId")
    void deleteByNoteId(Long noteId);

    /**
     * 删除某篇笔记与指定标签的单条关联。
     *
     * @param noteId 笔记 ID
     * @param tagId  标签 ID
     */
    @Modifying
    @Query("DELETE FROM NoteTagRelationPO r WHERE r.noteId = :noteId AND r.tagId = :tagId")
    void deleteByNoteIdAndTagId(Long noteId, Long tagId);

    /**
     * 检查关联是否已存在。
     *
     * @param noteId 笔记 ID
     * @param tagId  标签 ID
     * @return true 若关联存在
     */
    boolean existsByNoteIdAndTagId(Long noteId, Long tagId);

    /**
     * 查询包含全部指定标签的笔记 ID（逐标签 AND 过滤由业务层处理）。
     *
     * @param tagIds 标签 ID 列表
     * @return 笔记 ID 列表（含重复，用于 HAVING COUNT 场景）
     */
    @Query("SELECT r.noteId FROM NoteTagRelationPO r WHERE r.tagId IN :tagIds GROUP BY r.noteId HAVING COUNT(DISTINCT r.tagId) = :tagCount")
    List<Long> findNoteIdsHavingAllTags(List<Long> tagIds, long tagCount);
}
