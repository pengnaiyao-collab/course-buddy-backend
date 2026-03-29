package com.coursebuddy.service.impl;

import com.coursebuddy.auth.User;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.domain.po.VersionPO;
import com.coursebuddy.domain.vo.VersionDiffVO;
import com.coursebuddy.mapper.VersionMapper;
import com.coursebuddy.service.IVersionService;
import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class VersionServiceImpl implements IVersionService {

    private final VersionMapper versionRepository;

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

        versionRepository.insert(version);
        VersionPO saved = version;
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

    @Override
    @Transactional(readOnly = true)
    public VersionDiffVO compareVersions(String entityType, Long entityId,
                                         int versionA, int versionB) {
        VersionPO va = getVersion(entityType, entityId, versionA);
        VersionPO vb = getVersion(entityType, entityId, versionB);

        String contentA = va.getContent() != null ? va.getContent() : "";
        String contentB = vb.getContent() != null ? vb.getContent() : "";
        List<String> linesA = Arrays.asList(contentA.split("\\r?\\n", -1));
        List<String> linesB = Arrays.asList(contentB.split("\\r?\\n", -1));

        Patch<String> patch = DiffUtils.diff(linesA, linesB);

        String nameA = "v" + versionA;
        String nameB = "v" + versionB;
        List<String> diffLines = UnifiedDiffUtils.generateUnifiedDiff(nameA, nameB, linesA, patch, 3);

        long added = diffLines.stream().filter(l -> l.startsWith("+") && !l.startsWith("+++")).count();
        long removed = diffLines.stream().filter(l -> l.startsWith("-") && !l.startsWith("---")).count();

        return VersionDiffVO.builder()
                .entityType(entityType)
                .entityId(entityId)
                .versionA(versionA)
                .versionB(versionB)
                .diffLines(diffLines)
                .addedLines(added)
                .removedLines(removed)
                .build();
    }
}
