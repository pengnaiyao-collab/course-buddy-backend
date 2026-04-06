package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coursebuddy.domain.po.AssignmentSubmissionPO;
import com.coursebuddy.domain.vo.AssignmentSubmissionCountVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

/**
 * 作业提交映射器
 */
@Mapper
public interface AssignmentSubmissionMapper extends BaseMapper<AssignmentSubmissionPO> {

    @Select("SELECT * FROM assignment_submissions WHERE assignment_id = #{assignmentId}")
    IPage<AssignmentSubmissionPO> findByAssignmentId(Page<AssignmentSubmissionPO> page, @Param("assignmentId") Long assignmentId);

    @Select("SELECT * FROM assignment_submissions WHERE assignment_id = #{assignmentId} AND student_id = #{studentId}")
    Optional<AssignmentSubmissionPO> findByAssignmentIdAndStudentId(@Param("assignmentId") Long assignmentId, @Param("studentId") Long studentId);

        @Select("SELECT s.* FROM assignment_submissions s " +
            "JOIN assignments a ON a.id = s.assignment_id " +
            "WHERE a.course_id = #{courseId} AND a.deleted_at IS NULL AND s.student_id = #{studentId}")
        List<AssignmentSubmissionPO> findByCourseIdAndStudentId(@Param("courseId") Long courseId, @Param("studentId") Long studentId);

            @Select("SELECT s.assignment_id AS assignmentId, COUNT(*) AS submissionCount " +
                "FROM assignment_submissions s " +
                "JOIN assignments a ON a.id = s.assignment_id " +
                "WHERE a.course_id = #{courseId} AND a.deleted_at IS NULL " +
                "GROUP BY s.assignment_id")
            List<AssignmentSubmissionCountVO> findSubmissionCountsByCourse(@Param("courseId") Long courseId);

    @Select("SELECT COUNT(*) FROM assignment_submissions WHERE assignment_id = #{assignmentId}")
    long countByAssignmentId(@Param("assignmentId") Long assignmentId);

    @Select("SELECT COUNT(*) FROM assignment_submissions WHERE assignment_id = #{assignmentId} AND status = #{status}")
    long countByAssignmentIdAndStatus(@Param("assignmentId") Long assignmentId, @Param("status") String status);

    @Select("SELECT AVG(score) FROM assignment_submissions WHERE assignment_id = #{assignmentId} AND score IS NOT NULL")
    Double findAverageScoreByAssignmentId(@Param("assignmentId") Long assignmentId);
}
