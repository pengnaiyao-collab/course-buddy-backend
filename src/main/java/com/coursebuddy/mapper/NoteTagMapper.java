package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coursebuddy.domain.po.NoteTagPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface NoteTagMapper extends BaseMapper<NoteTagPO> {

    @Select("SELECT * FROM note_tags WHERE user_id = #{userId} ORDER BY use_count DESC")
    List<NoteTagPO> findByUserIdOrderByUseCountDesc(@Param("userId") Long userId);

    @Select("SELECT * FROM note_tags WHERE id = #{id} AND user_id = #{userId}")
    Optional<NoteTagPO> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Select("SELECT * FROM note_tags WHERE user_id = #{userId} AND name = #{name}")
    Optional<NoteTagPO> findByUserIdAndName(@Param("userId") Long userId, @Param("name") String name);

    @Select("SELECT * FROM note_tags WHERE user_id = #{userId} AND LOWER(name) LIKE LOWER(CONCAT('%', #{keyword}, '%'))")
    List<NoteTagPO> findByUserIdAndNameContainingIgnoreCase(@Param("userId") Long userId, @Param("keyword") String keyword);
}
