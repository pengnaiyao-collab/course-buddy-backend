package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coursebuddy.domain.po.AuditLogPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLogPO> {

    @Select("SELECT * FROM audit_logs WHERE entity_type = #{entityType} AND entity_id = #{entityId}")
    IPage<AuditLogPO> findByEntityTypeAndEntityId(Page<AuditLogPO> page, @Param("entityType") String entityType, @Param("entityId") Long entityId);

    @Select("SELECT * FROM audit_logs WHERE operator_id = #{operatorId}")
    IPage<AuditLogPO> findByOperatorId(Page<AuditLogPO> page, @Param("operatorId") Long operatorId);
}
