package com.coursebuddy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coursebuddy.domain.po.OcrResultPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface OcrResultMapper extends BaseMapper<OcrResultPO> {

    @Select("SELECT * FROM ocr_results WHERE object_name = #{objectName}")
    Optional<OcrResultPO> findByObjectName(@Param("objectName") String objectName);

    @Select("SELECT * FROM ocr_results WHERE file_upload_id = #{fileUploadId}")
    List<OcrResultPO> findByFileUploadId(@Param("fileUploadId") Long fileUploadId);

    @Select("SELECT * FROM ocr_results WHERE status = #{status}")
    List<OcrResultPO> findByStatus(@Param("status") String status);
}
