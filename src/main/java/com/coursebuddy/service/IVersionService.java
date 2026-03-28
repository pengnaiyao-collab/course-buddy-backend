package com.coursebuddy.service;

import com.coursebuddy.domain.po.VersionPO;
import com.coursebuddy.domain.vo.VersionDiffVO;

import java.util.List;

public interface IVersionService {

    VersionPO saveVersion(String entityType, Long entityId, String content, String description);

    List<VersionPO> listVersions(String entityType, Long entityId);

    VersionPO getVersion(String entityType, Long entityId, int versionNumber);

    /**
     * Compare two versions and return a unified diff.
     *
     * @param entityType  type of entity
     * @param entityId    entity identifier
     * @param versionA    older version number
     * @param versionB    newer version number
     * @return diff result with added/removed lines
     */
    VersionDiffVO compareVersions(String entityType, Long entityId, int versionA, int versionB);
}
