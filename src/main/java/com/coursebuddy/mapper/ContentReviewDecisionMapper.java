package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coursebuddy.domain.po.ContentReviewDecisionPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ContentReviewDecisionMapper extends BaseMapper<ContentReviewDecisionPO> {

    @Select("SELECT * FROM content_review_decisions WHERE review_id = #{reviewId} ORDER BY created_at ASC")
    List<ContentReviewDecisionPO> findByReviewIdOrderByCreatedAtAsc(@Param("reviewId") Long reviewId);

    @Select("SELECT * FROM content_review_decisions WHERE review_id = #{reviewId} AND reviewer_id = #{reviewerId}")
    Optional<ContentReviewDecisionPO> findByReviewIdAndReviewerId(@Param("reviewId") Long reviewId, @Param("reviewerId") Long reviewerId);

    @Select("SELECT COUNT(*) FROM content_review_decisions WHERE review_id = #{reviewId} AND decision = #{decision}")
    long countByReviewIdAndDecision(@Param("reviewId") Long reviewId, @Param("decision") String decision);
}
