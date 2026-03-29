package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coursebuddy.domain.po.ProjectMemberPO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ProjectMemberMapper extends BaseMapper<ProjectMemberPO> {

    @Select("SELECT * FROM project_members WHERE project_id = #{projectId}")
    List<ProjectMemberPO> findByProjectId(@Param("projectId") Long projectId);

    @Select("SELECT * FROM project_members WHERE project_id = #{projectId}")
    IPage<ProjectMemberPO> findPageByProjectId(Page<ProjectMemberPO> page, @Param("projectId") Long projectId);

    @Select("SELECT * FROM project_members WHERE project_id = #{projectId} AND user_id = #{userId}")
    Optional<ProjectMemberPO> findByProjectIdAndUserId(@Param("projectId") Long projectId, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) > 0 FROM project_members WHERE project_id = #{projectId} AND user_id = #{userId}")
    boolean existsByProjectIdAndUserId(@Param("projectId") Long projectId, @Param("userId") Long userId);

    @Delete("DELETE FROM project_members WHERE project_id = #{projectId} AND user_id = #{userId}")
    void deleteByProjectIdAndUserId(@Param("projectId") Long projectId, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM project_members WHERE project_id = #{projectId}")
    long countByProjectId(@Param("projectId") Long projectId);
}
