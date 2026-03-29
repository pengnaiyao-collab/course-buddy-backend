package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coursebuddy.domain.po.VersionPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface VersionMapper extends BaseMapper<VersionPO> {

    @Select("SELECT * FROM versions WHERE entity_type = #{entityType} AND entity_id = #{entityId} ORDER BY version_number DESC")
    List<VersionPO> findByEntityTypeAndEntityIdOrderByVersionNumberDesc(
            @Param("entityType") String entityType, @Param("entityId") Long entityId);

    @Select("SELECT * FROM versions WHERE entity_type = #{entityType} AND entity_id = #{entityId} AND version_number = #{versionNumber}")
    Optional<VersionPO> findByEntityTypeAndEntityIdAndVersionNumber(
            @Param("entityType") String entityType, @Param("entityId") Long entityId, @Param("versionNumber") int versionNumber);

    @Select("SELECT COALESCE(MAX(version_number), 0) FROM versions WHERE entity_type = #{entityType} AND entity_id = #{entityId}")
    int findMaxVersionNumber(@Param("entityType") String entityType, @Param("entityId") Long entityId);
}
