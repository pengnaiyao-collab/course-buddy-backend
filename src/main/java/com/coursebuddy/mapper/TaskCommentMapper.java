package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coursebuddy.domain.po.TaskCommentPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TaskCommentMapper extends BaseMapper<TaskCommentPO> {

    @Select("SELECT * FROM task_comments WHERE task_id = #{taskId} AND deleted_at IS NULL")
    IPage<TaskCommentPO> findByTaskIdAndDeletedAtIsNull(Page<TaskCommentPO> page, @Param("taskId") Long taskId);

    @Select("SELECT COUNT(*) FROM task_comments WHERE task_id = #{taskId} AND deleted_at IS NULL")
    long countByTaskIdAndDeletedAtIsNull(@Param("taskId") Long taskId);
}
