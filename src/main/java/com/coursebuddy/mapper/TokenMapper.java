package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coursebuddy.domain.po.TokenPO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

/**
 * 令牌映射器
 */
@Mapper
public interface TokenMapper extends BaseMapper<TokenPO> {

    @Select("SELECT * FROM tokens WHERE refresh_token = #{refreshToken}")
    Optional<TokenPO> findByRefreshToken(@Param("refreshToken") String refreshToken);

    @Select("SELECT * FROM tokens WHERE access_token = #{accessToken}")
    Optional<TokenPO> findByAccessToken(@Param("accessToken") String accessToken);

    @Select("SELECT * FROM tokens WHERE user_id = #{userId}")
    List<TokenPO> findByUserId(@Param("userId") Long userId);

    @Delete("DELETE FROM tokens WHERE user_id = #{userId} AND is_revoked = true")
    void deleteByUserIdAndIsRevokedTrue(@Param("userId") Long userId);
}
