package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coursebuddy.domain.po.UserPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

/**
 * 用户映射器
 */
@Mapper
public interface UserMapper extends BaseMapper<UserPO> {

    @Select("SELECT * FROM users WHERE username = #{username}")
    Optional<UserPO> findByUsername(@Param("username") String username);

    @Select("SELECT COUNT(*) > 0 FROM users WHERE username = #{username}")
    boolean existsByUsername(@Param("username") String username);

    @Select("SELECT * FROM users WHERE role = #{role}")
    java.util.List<UserPO> findByRole(@Param("role") String role);

    @Select("SELECT * FROM users WHERE status = #{status}")
    IPage<UserPO> findByStatus(Page<UserPO> page, @Param("status") String status);

    @Select("SELECT * FROM users WHERE role = #{role} AND status = #{status}")
    IPage<UserPO> findByRoleAndStatus(Page<UserPO> page, @Param("role") String role, @Param("status") String status);

    @Select("SELECT * FROM users WHERE username LIKE CONCAT('%', #{keyword}, '%') OR real_name LIKE CONCAT('%', #{keyword}, '%')")
    IPage<UserPO> searchByKeyword(Page<UserPO> page, @Param("keyword") String keyword);
}
