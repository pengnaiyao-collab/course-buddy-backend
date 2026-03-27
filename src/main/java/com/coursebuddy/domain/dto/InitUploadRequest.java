package com.coursebuddy.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class InitUploadRequest {

    @NotBlank(message = "文件名不能为空")
    private String fileName;

    @Positive(message = "文件大小必须大于0")
    private long fileSize;
}
