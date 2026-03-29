package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coursebuddy.domain.po.UserSettingsPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

@Mapper
public interface UserSettingsMapper extends BaseMapper<UserSettingsPO> {

    @Select("SELECT * FROM user_settings WHERE user_id = #{userId}")
    Optional<UserSettingsPO> findByUserId(@Param("userId") Long userId);
}
