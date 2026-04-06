package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coursebuddy.domain.po.ContentReviewPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

/**
 * 内容审核映射器
 */
@Mapper
public interface ContentReviewMapper extends BaseMapper<ContentReviewPO> {

    @Select("SELECT * FROM content_reviews WHERE status = #{status}")
    IPage<ContentReviewPO> findByStatus(Page<ContentReviewPO> page, @Param("status") String status);

    @Select("<script>SELECT * FROM content_reviews WHERE status IN <foreach collection='statuses' item='status' open='(' separator=',' close=')'>#{status}</foreach></script>")
    IPage<ContentReviewPO> findByStatusIn(Page<ContentReviewPO> page, @Param("statuses") Set<String> statuses);

    @Select("SELECT * FROM content_reviews WHERE content_type = #{contentType} AND content_id = #{contentId}")
    List<ContentReviewPO> findByContentTypeAndContentId(@Param("contentType") String contentType, @Param("contentId") Long contentId);

    @Select("SELECT * FROM content_reviews WHERE reviewer_id = #{reviewerId}")
    List<ContentReviewPO> findByReviewerId(@Param("reviewerId") Long reviewerId);
}
