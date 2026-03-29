package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coursebuddy.domain.po.TeamInvitationPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface TeamInvitationMapper extends BaseMapper<TeamInvitationPO> {

    @Select("SELECT * FROM team_invitations WHERE invited_user_id = #{invitedUserId} AND status = #{status}")
    List<TeamInvitationPO> findByInvitedUserIdAndStatus(@Param("invitedUserId") Long invitedUserId, @Param("status") String status);

    @Select("SELECT * FROM team_invitations WHERE team_id = #{teamId} AND invited_user_id = #{invitedUserId}")
    Optional<TeamInvitationPO> findByTeamIdAndInvitedUserId(@Param("teamId") Long teamId, @Param("invitedUserId") Long invitedUserId);

    @Select("SELECT COUNT(*) > 0 FROM team_invitations WHERE team_id = #{teamId} AND invited_user_id = #{invitedUserId} AND status = #{status}")
    boolean existsByTeamIdAndInvitedUserIdAndStatus(@Param("teamId") Long teamId, @Param("invitedUserId") Long invitedUserId, @Param("status") String status);
}
