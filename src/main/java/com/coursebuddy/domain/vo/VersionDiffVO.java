package com.coursebuddy.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 版本差异视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VersionDiffVO {
    private String entityType;
    private Long entityId;
    private int versionA;
    private int versionB;
    /** 统一差异格式（Unified Diff）的逐行列表 */
    private List<String> diffLines;
    /** 新增行数 */
    private long addedLines;
    /** 删除行数 */
    private long removedLines;
}
