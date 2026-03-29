package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coursebuddy.domain.po.CourseAdminVotePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CourseAdminVoteMapper extends BaseMapper<CourseAdminVotePO> {

    @Select("SELECT * FROM course_admin_votes WHERE course_id = #{courseId}")
    List<CourseAdminVotePO> findByCourseId(@Param("courseId") Long courseId);

    @Select("SELECT COUNT(*) FROM course_admin_votes WHERE course_id = #{courseId} AND candidate_user_id = #{candidateUserId}")
    long countByCourseIdAndCandidateUserId(@Param("courseId") Long courseId, @Param("candidateUserId") Long candidateUserId);

    @Select("SELECT * FROM course_admin_votes WHERE course_id = #{courseId} AND candidate_user_id = #{candidateUserId} AND voter_user_id = #{voterUserId}")
    Optional<CourseAdminVotePO> findByCourseIdAndCandidateUserIdAndVoterUserId(
            @Param("courseId") Long courseId, @Param("candidateUserId") Long candidateUserId, @Param("voterUserId") Long voterUserId);
}
