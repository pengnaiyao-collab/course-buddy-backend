package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coursebuddy.domain.po.WebImportPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface WebImportMapper extends BaseMapper<WebImportPO> {

    @Select("SELECT * FROM web_imports WHERE course_id = #{courseId}")
    IPage<WebImportPO> findByCourseId(Page<WebImportPO> page, @Param("courseId") Long courseId);

    @Select("SELECT * FROM web_imports WHERE course_id = #{courseId} AND status = #{status}")
    IPage<WebImportPO> findByCourseIdAndStatus(Page<WebImportPO> page, @Param("courseId") Long courseId, @Param("status") String status);
}
