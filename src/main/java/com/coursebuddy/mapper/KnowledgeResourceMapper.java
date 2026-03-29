package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coursebuddy.domain.po.KnowledgeResourcePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface KnowledgeResourceMapper extends BaseMapper<KnowledgeResourcePO> {

    @Select("SELECT * FROM knowledge_resources WHERE knowledge_item_id = #{knowledgeItemId} ORDER BY created_at DESC")
    List<KnowledgeResourcePO> findByKnowledgeItemIdOrderByCreatedAtDesc(@Param("knowledgeItemId") Long knowledgeItemId);
}
