package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coursebuddy.domain.po.CollaborationProjectPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CollaborationProjectMapper extends BaseMapper<CollaborationProjectPO> {

    @Select("SELECT * FROM collaboration_projects WHERE owner_id = #{ownerId}")
    IPage<CollaborationProjectPO> findByOwnerId(Page<CollaborationProjectPO> page, @Param("ownerId") Long ownerId);

    @Select("SELECT * FROM collaboration_projects WHERE course_id = #{courseId}")
    IPage<CollaborationProjectPO> findByCourseId(Page<CollaborationProjectPO> page, @Param("courseId") Long courseId);
}
