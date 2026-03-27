package com.coursebuddy.service.impl;

import com.coursebuddy.auth.User;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.domain.po.VersionPO;
import com.coursebuddy.repository.VersionRepository;
import com.coursebuddy.service.IVersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class VersionServiceImpl implements IVersionService {

    private final VersionRepository versionRepository;

    @Override
    @Transactional
    public VersionPO saveVersion(String entityType, Long entityId,
                                  String content, String description) {
        int nextVersion = versionRepository.findMaxVersionNumber(entityType, entityId) + 1;

        Long userId = null;
        try {
            User user = SecurityUtils.getCurrentUser();
            if (user != null) userId = user.getId();
        } catch (Exception ignored) {
        }

        VersionPO version = VersionPO.builder()
                .entityType(entityType)
                .entityId(entityId)
                .versionNumber(nextVersion)
                .content(content)
                .createdBy(userId)
                .description(description)
                .build();

        VersionPO saved = versionRepository.save(version);
        log.info("Saved version {} for {}/{}", nextVersion, entityType, entityId);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VersionPO> listVersions(String entityType, Long entityId) {
        return versionRepository
                .findByEntityTypeAndEntityIdOrderByVersionNumberDesc(entityType, entityId);
    }

    @Override
    @Transactional(readOnly = true)
    public VersionPO getVersion(String entityType, Long entityId, int versionNumber) {
        return versionRepository
                .findByEntityTypeAndEntityIdAndVersionNumber(entityType, entityId, versionNumber)
                .orElseThrow(() -> new BusinessException(404,
                        "Version not found: " + entityType + "/" + entityId + "/v" + versionNumber));
    }
}
