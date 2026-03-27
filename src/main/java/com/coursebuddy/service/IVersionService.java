package com.coursebuddy.service;

import com.coursebuddy.domain.po.VersionPO;

import java.util.List;

public interface IVersionService {

    VersionPO saveVersion(String entityType, Long entityId, String content, String description);

    List<VersionPO> listVersions(String entityType, Long entityId);

    VersionPO getVersion(String entityType, Long entityId, int versionNumber);
}
