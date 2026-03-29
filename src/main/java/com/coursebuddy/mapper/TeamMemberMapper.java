package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coursebuddy.domain.po.TeamMemberPO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface TeamMemberMapper extends BaseMapper<TeamMemberPO> {

    @Select("SELECT * FROM team_members WHERE team_id = #{teamId}")
    List<TeamMemberPO> findByTeamId(@Param("teamId") Long teamId);

    @Select("SELECT * FROM team_members WHERE team_id = #{teamId} AND user_id = #{userId}")
    Optional<TeamMemberPO> findByTeamIdAndUserId(@Param("teamId") Long teamId, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) > 0 FROM team_members WHERE team_id = #{teamId} AND user_id = #{userId}")
    boolean existsByTeamIdAndUserId(@Param("teamId") Long teamId, @Param("userId") Long userId);

    @Delete("DELETE FROM team_members WHERE team_id = #{teamId} AND user_id = #{userId}")
    void deleteByTeamIdAndUserId(@Param("teamId") Long teamId, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM team_members WHERE team_id = #{teamId}")
    long countByTeamId(@Param("teamId") Long teamId);
}
