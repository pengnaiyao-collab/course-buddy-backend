package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coursebuddy.domain.po.NotePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NoteMapper extends BaseMapper<NotePO> {

    @Select("SELECT * FROM notes WHERE user_id = #{userId} AND is_deleted = false")
    IPage<NotePO> findByUserIdAndIsDeletedFalse(Page<NotePO> page, @Param("userId") Long userId);

    @Select("SELECT * FROM notes WHERE user_id = #{userId} AND course_id = #{courseId} AND is_deleted = false")
    IPage<NotePO> findByUserIdAndCourseIdAndIsDeletedFalse(Page<NotePO> page, @Param("userId") Long userId, @Param("courseId") Long courseId);

    @Select("SELECT * FROM notes WHERE user_id = #{userId} AND category_id = #{categoryId} AND is_deleted = false")
    IPage<NotePO> findByUserIdAndCategoryIdAndIsDeletedFalse(Page<NotePO> page, @Param("userId") Long userId, @Param("categoryId") Long categoryId);

    @Select("SELECT * FROM notes WHERE user_id = #{userId} AND LOWER(title) LIKE LOWER(CONCAT('%', #{keyword}, '%')) AND is_deleted = false")
    IPage<NotePO> findByUserIdAndTitleContainingIgnoreCaseAndIsDeletedFalse(Page<NotePO> page, @Param("userId") Long userId, @Param("keyword") String keyword);

    @Select("<script>SELECT * FROM notes WHERE user_id = #{userId} AND id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> AND is_deleted = false</script>")
    IPage<NotePO> findByUserIdAndIdInAndIsDeletedFalse(Page<NotePO> page, @Param("userId") Long userId, @Param("ids") List<Long> ids);
}
