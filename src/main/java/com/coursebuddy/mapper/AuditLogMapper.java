package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coursebuddy.domain.po.AuditLogPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 审计日志映射器
 */
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLogPO> {

    @Select("SELECT * FROM audit_logs WHERE entity_type = #{entityType} AND entity_id = #{entityId}")
    IPage<AuditLogPO> findByEntityTypeAndEntityId(Page<AuditLogPO> page, @Param("entityType") String entityType, @Param("entityId") Long entityId);

    @Select("SELECT * FROM audit_logs WHERE operator_id = #{operatorId}")
    IPage<AuditLogPO> findByOperatorId(Page<AuditLogPO> page, @Param("operatorId") Long operatorId);

    @Select("<script>" +
            "SELECT * FROM audit_logs WHERE entity_id = #{entityId} " +
            "AND entity_type IN " +
            "<foreach collection='entityTypes' item='type' open='(' separator=',' close=')'>#{type}</foreach> " +
            "ORDER BY created_at DESC" +
            "</script>")
    IPage<AuditLogPO> findByEntityTypesAndEntityId(Page<AuditLogPO> page,
                                                   @Param("entityTypes") List<String> entityTypes,
                                                   @Param("entityId") Long entityId);
}
