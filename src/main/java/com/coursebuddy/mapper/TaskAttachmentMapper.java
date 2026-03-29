package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coursebuddy.domain.po.TaskAttachmentPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TaskAttachmentMapper extends BaseMapper<TaskAttachmentPO> {

    @Select("SELECT * FROM task_attachments WHERE task_id = #{taskId}")
    List<TaskAttachmentPO> findByTaskId(@Param("taskId") Long taskId);

    @Select("SELECT COUNT(*) FROM task_attachments WHERE task_id = #{taskId}")
    long countByTaskId(@Param("taskId") Long taskId);
}
