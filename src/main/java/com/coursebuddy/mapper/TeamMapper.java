package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coursebuddy.domain.po.TeamPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TeamMapper extends BaseMapper<TeamPO> {

    @Select("SELECT * FROM teams WHERE owner_id = #{ownerId}")
    IPage<TeamPO> findByOwnerId(Page<TeamPO> page, @Param("ownerId") Long ownerId);

    @Select("SELECT t.* FROM teams t JOIN team_members tm ON t.id = tm.team_id WHERE tm.user_id = #{userId}")
    IPage<TeamPO> findByMemberId(Page<TeamPO> page, @Param("userId") Long userId);

    @Select("SELECT * FROM teams WHERE course_id = #{courseId}")
    IPage<TeamPO> findByCourseId(Page<TeamPO> page, @Param("courseId") Long courseId);
}
