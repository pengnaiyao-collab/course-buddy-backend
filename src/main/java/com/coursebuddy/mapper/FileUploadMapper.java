package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coursebuddy.domain.po.FileUploadPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

/**
 * 文件上传映射器
 */
@Mapper
public interface FileUploadMapper extends BaseMapper<FileUploadPO> {

    @Select("SELECT * FROM file_uploads WHERE object_name = #{objectName} AND is_deleted = false")
    Optional<FileUploadPO> findByObjectNameAndIsDeletedFalse(@Param("objectName") String objectName);
}
