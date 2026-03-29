package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coursebuddy.domain.po.KnowledgeItemPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface KnowledgeItemMapper extends BaseMapper<KnowledgeItemPO> {

    @Select("SELECT * FROM knowledge_items WHERE course_id = #{courseId}")
    IPage<KnowledgeItemPO> findPageByCourseId(Page<KnowledgeItemPO> page, @Param("courseId") Long courseId);

    @Select("SELECT * FROM knowledge_items WHERE course_id = #{courseId} AND LOWER(title) LIKE LOWER(CONCAT('%', #{keyword}, '%'))")
    IPage<KnowledgeItemPO> findByCourseIdAndTitleContainingIgnoreCase(Page<KnowledgeItemPO> page, @Param("courseId") Long courseId, @Param("keyword") String keyword);

    @Select("SELECT * FROM knowledge_items WHERE course_id = #{courseId} AND category = #{category}")
    IPage<KnowledgeItemPO> findByCourseIdAndCategory(Page<KnowledgeItemPO> page, @Param("courseId") Long courseId, @Param("category") String category);

    @Select("SELECT * FROM knowledge_items WHERE course_id = #{courseId}")
    List<KnowledgeItemPO> findByCourseId(@Param("courseId") Long courseId);

    @Select("<script>" +
            "SELECT * FROM knowledge_items WHERE course_id = #{courseId} " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND LOWER(CONCAT(COALESCE(title,''), ' ', COALESCE(description,''), ' ', COALESCE(extracted_text,''), ' ', COALESCE(tags,''))) " +
            "LIKE LOWER(CONCAT('%', #{keyword}, '%'))" +
            "</if>" +
            "</script>")
    List<KnowledgeItemPO> searchByCourseAndKeyword(@Param("courseId") Long courseId, @Param("keyword") String keyword);
}
