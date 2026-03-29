package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coursebuddy.domain.po.CollaborationLogPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CollaborationLogMapper extends BaseMapper<CollaborationLogPO> {

    @Select("SELECT * FROM collaboration_logs WHERE project_id = #{projectId}")
    IPage<CollaborationLogPO> findByProjectId(Page<CollaborationLogPO> page, @Param("projectId") Long projectId);

    @Select("SELECT * FROM collaboration_logs WHERE project_id = #{projectId} AND user_id = #{userId}")
    List<CollaborationLogPO> findByProjectIdAndUserId(@Param("projectId") Long projectId, @Param("userId") Long userId);
}
