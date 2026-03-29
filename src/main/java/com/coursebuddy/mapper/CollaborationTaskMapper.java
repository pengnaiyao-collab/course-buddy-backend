package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coursebuddy.domain.po.CollaborationTaskPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CollaborationTaskMapper extends BaseMapper<CollaborationTaskPO> {

    @Select("SELECT * FROM collaboration_tasks WHERE project_id = #{projectId}")
    IPage<CollaborationTaskPO> findByProjectId(Page<CollaborationTaskPO> page, @Param("projectId") Long projectId);

    @Select("SELECT * FROM collaboration_tasks WHERE project_id = #{projectId} AND status = #{status}")
    IPage<CollaborationTaskPO> findByProjectIdAndStatus(Page<CollaborationTaskPO> page, @Param("projectId") Long projectId, @Param("status") String status);

    @Select("SELECT * FROM collaboration_tasks WHERE assignee_id = #{assigneeId}")
    IPage<CollaborationTaskPO> findByAssigneeId(Page<CollaborationTaskPO> page, @Param("assigneeId") Long assigneeId);

    @Select("SELECT * FROM collaboration_tasks WHERE assignee_id = #{assigneeId} AND status = #{status}")
    IPage<CollaborationTaskPO> findByAssigneeIdAndStatus(Page<CollaborationTaskPO> page, @Param("assigneeId") Long assigneeId, @Param("status") String status);

    @Select("SELECT COUNT(*) FROM collaboration_tasks WHERE project_id = #{projectId} AND status = #{status}")
    long countByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") String status);

    @Select("SELECT COUNT(*) FROM collaboration_tasks WHERE project_id = #{projectId}")
    long countByProjectId(@Param("projectId") Long projectId);
}
