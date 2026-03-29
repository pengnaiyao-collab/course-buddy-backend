package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coursebuddy.domain.po.NotificationPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface NotificationMapper extends BaseMapper<NotificationPO> {

    @Select("SELECT * FROM notifications WHERE user_id = #{userId}")
    IPage<NotificationPO> findByUserId(Page<NotificationPO> page, @Param("userId") Long userId);

    @Select("SELECT * FROM notifications WHERE user_id = #{userId} AND is_read = #{isRead}")
    IPage<NotificationPO> findByUserIdAndIsRead(Page<NotificationPO> page, @Param("userId") Long userId, @Param("isRead") Boolean isRead);

    @Select("SELECT * FROM notifications WHERE user_id = #{userId} AND is_read = #{isRead} AND type = #{type}")
    IPage<NotificationPO> findByUserIdAndIsReadAndType(Page<NotificationPO> page, @Param("userId") Long userId, @Param("isRead") Boolean isRead, @Param("type") String type);

    @Select("SELECT * FROM notifications WHERE user_id = #{userId} AND type = #{type}")
    IPage<NotificationPO> findByUserIdAndType(Page<NotificationPO> page, @Param("userId") Long userId, @Param("type") String type);

    @Select("SELECT COUNT(*) FROM notifications WHERE user_id = #{userId} AND is_read = #{isRead}")
    long countByUserIdAndIsRead(@Param("userId") Long userId, @Param("isRead") Boolean isRead);

    @Update("UPDATE notifications SET is_read = true WHERE user_id = #{userId}")
    int markAllReadByUserId(@Param("userId") Long userId);
}
