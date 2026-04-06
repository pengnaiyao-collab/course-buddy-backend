package com.coursebuddy.service;

import com.coursebuddy.domain.po.VersionPO;
import com.coursebuddy.domain.vo.VersionDiffVO;

import java.util.List;

/**
 * 版本服务
 */
public interface IVersionService {

    VersionPO saveVersion(String entityType, Long entityId, String content, String description);

    List<VersionPO> listVersions(String entityType, Long entityId);

    VersionPO getVersion(String entityType, Long entityId, int versionNumber);

    /**
     * 对比两个版本并返回统一差异格式（Unified Diff）。
     *
     * @param entityType 实体类型
     * @param entityId 实体标识
     * @param versionA 较旧的版本号
     * @param versionB 较新的版本号
     * @return 包含增删行的差异结果
     */
    VersionDiffVO compareVersions(String entityType, Long entityId, int versionA, int versionB);
}
