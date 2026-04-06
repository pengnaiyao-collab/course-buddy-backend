package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coursebuddy.domain.po.CoursePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 课程目录映射器
 */
@Mapper
public interface CourseCatalogMapper extends BaseMapper<CoursePO> {

    @Select("SELECT * FROM course_catalog WHERE code = #{code}")
    Optional<CoursePO> findByCode(@Param("code") String code);

    @Select("SELECT * FROM course_catalog WHERE deleted_at IS NULL")
    IPage<CoursePO> findByDeletedAtIsNull(Page<CoursePO> page);

    @Select("SELECT * FROM course_catalog WHERE instructor_id = #{instructorId} AND deleted_at IS NULL")
    IPage<CoursePO> findByInstructorIdAndDeletedAtIsNull(Page<CoursePO> page, @Param("instructorId") Long instructorId);

    @Select("SELECT * FROM course_catalog WHERE id IN (" +
            "  SELECT course_id FROM course_enrollments WHERE user_id = #{studentId} AND status IN ('ACTIVE', 'COMPLETED')" +
            ") AND deleted_at IS NULL")
    java.util.List<CoursePO> findByStudentIdAndDeletedAtIsNull(@Param("studentId") Long studentId);

    @Select("SELECT * FROM course_catalog WHERE status = #{status} AND deleted_at IS NULL")
    IPage<CoursePO> findByStatusAndDeletedAtIsNull(Page<CoursePO> page, @Param("status") String status);

    @Select("<script>" +
            "SELECT * FROM course_catalog WHERE deleted_at IS NULL " +
            "<if test='keyword != null'> AND (LOWER(name) LIKE LOWER(CONCAT('%', #{keyword}, '%')) OR LOWER(code) LIKE LOWER(CONCAT('%', #{keyword}, '%')))</if> " +
            "<if test='level != null'> AND level = #{level}</if>" +
            "</script>")
    IPage<CoursePO> searchCourses(Page<CoursePO> page, @Param("keyword") String keyword, @Param("level") String level);

    @Update("UPDATE course_catalog SET enrolled_count = enrolled_count + 1 WHERE id = #{id}")
    void incrementEnrolledCount(@Param("id") Long id);

    @Update("UPDATE course_catalog SET enrolled_count = enrolled_count - 1 WHERE id = #{id} AND enrolled_count > 0")
    void decrementEnrolledCount(@Param("id") Long id);

        @Update("UPDATE course_catalog SET deleted_at = #{deletedAt} WHERE id = #{id} AND deleted_at IS NULL")
        int markDeleted(@Param("id") Long id, @Param("deletedAt") LocalDateTime deletedAt);
}
