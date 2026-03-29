package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coursebuddy.domain.po.KnowledgeAssociationPO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface KnowledgeAssociationMapper extends BaseMapper<KnowledgeAssociationPO> {

    @Select("SELECT * FROM knowledge_associations WHERE source_id = #{sourceId}")
    List<KnowledgeAssociationPO> findBySourceId(@Param("sourceId") Long sourceId);

    @Select("SELECT * FROM knowledge_associations WHERE target_id = #{targetId}")
    List<KnowledgeAssociationPO> findByTargetId(@Param("targetId") Long targetId);

    @Select("SELECT * FROM knowledge_associations WHERE source_id = #{sourceId} AND target_id = #{targetId}")
    Optional<KnowledgeAssociationPO> findBySourceIdAndTargetId(@Param("sourceId") Long sourceId, @Param("targetId") Long targetId);

    @Delete("DELETE FROM knowledge_associations WHERE source_id = #{sourceId} AND target_id = #{targetId}")
    void deleteBySourceIdAndTargetId(@Param("sourceId") Long sourceId, @Param("targetId") Long targetId);

    @Select("SELECT COUNT(*) > 0 FROM knowledge_associations WHERE source_id = #{sourceId} AND target_id = #{targetId}")
    boolean existsBySourceIdAndTargetId(@Param("sourceId") Long sourceId, @Param("targetId") Long targetId);

    @Select("<script>SELECT * FROM knowledge_associations WHERE source_id IN <foreach collection='sourceIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> OR target_id IN <foreach collection='targetIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    List<KnowledgeAssociationPO> findBySourceIdInOrTargetIdIn(@Param("sourceIds") List<Long> sourceIds, @Param("targetIds") List<Long> targetIds);
}
