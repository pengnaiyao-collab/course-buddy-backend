package com.coursebuddy.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.coursebuddy.common.MybatisPlusPageUtils;
import com.coursebuddy.auth.User;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.domain.po.AuditLogPO;
import com.coursebuddy.domain.dto.KnowledgeAnalyzeDTO;
import com.coursebuddy.domain.dto.KnowledgeItemDTO;
import com.coursebuddy.domain.dto.KnowledgeResourceDTO;
import com.coursebuddy.domain.po.KnowledgeAssociationPO;
import com.coursebuddy.domain.po.KnowledgeItemPO;
import com.coursebuddy.domain.po.KnowledgeResourcePO;
import com.coursebuddy.domain.po.VersionPO;
import com.coursebuddy.domain.vo.KnowledgeAnalyzeResultVO;
import com.coursebuddy.domain.vo.KnowledgeGraphVO;
import com.coursebuddy.domain.vo.KnowledgeItemVO;
import com.coursebuddy.domain.vo.KnowledgeResourceVO;
import com.coursebuddy.converter.KnowledgeItemConverter;
import com.coursebuddy.mapper.AuditLogMapper;
import com.coursebuddy.mapper.KnowledgeAssociationMapper;
import com.coursebuddy.mapper.KnowledgeItemMapper;
import com.coursebuddy.mapper.KnowledgeResourceMapper;
import com.coursebuddy.service.IKnowledgeBaseService;
import com.coursebuddy.service.IVersionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class KnowledgeBaseServiceImpl implements IKnowledgeBaseService {

    private final KnowledgeItemMapper repository;
    private final KnowledgeItemConverter mapper;
    private final KnowledgeAssociationMapper associationRepository;
    private final KnowledgeResourceMapper knowledgeResourceRepository;
    private final IVersionService versionService;
    private final AuditLogMapper auditLogRepository;
    private final ObjectMapper objectMapper;

    private static final Pattern SPLIT_PATTERN = Pattern.compile("(?<=[。！？.!?])\\s+|\\n{2,}");

    @Override
    @Transactional
    public KnowledgeItemVO create(KnowledgeItemDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        KnowledgeItemPO po = mapper.dtoToPo(dto);
        po.setCreatedBy(currentUser.getId());
        repository.insert(po);
        saveSnapshotVersion(po, "CREATE");
        writeAudit("KNOWLEDGE_ITEM", po.getId(), "CREATE", null, snapshotJson(po));
        return mapper.poToVo(po);
    }

    @Override
    @Transactional
    public KnowledgeItemVO createForCourse(Long courseId, KnowledgeItemDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        KnowledgeItemPO po = mapper.dtoToPo(dto);
        po.setCourseId(courseId);
        po.setCreatedBy(currentUser.getId());
        repository.insert(po);
        saveSnapshotVersion(po, "CREATE");
        writeAudit("KNOWLEDGE_ITEM", po.getId(), "CREATE", null, snapshotJson(po));
        return mapper.poToVo(po);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KnowledgeItemVO> listByCourse(Long courseId, Pageable pageable) {
        IPage<KnowledgeItemPO> poPage = repository.findByCourseId(
                MybatisPlusPageUtils.toMpPage(pageable), courseId);
        return mapper.poPageToVoPage(MybatisPlusPageUtils.toSpringPage(poPage, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public KnowledgeItemVO getById(Long id) {
        KnowledgeItemPO po = repository.selectById(id);
        if (po == null) {
            throw new BusinessException(404, "Knowledge item not found");
        }
        return mapper.poToVo(po);
    }

    @Override
    @Transactional
    public KnowledgeItemVO update(Long id, KnowledgeItemDTO dto) {
        KnowledgeItemPO existing = repository.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "Knowledge item not found");
        }
        String oldValue = snapshotJson(existing);
        existing.setTitle(dto.getTitle());
        existing.setDescription(dto.getDescription());
        existing.setContent(dto.getContent());
        existing.setFileUrl(dto.getFileUrl());
        existing.setFileType(dto.getFileType());
        existing.setCategory(dto.getCategory());
        existing.setTags(dto.getTags());
        if (dto.getSourceType() != null && !dto.getSourceType().isBlank()) {
            existing.setSourceType(dto.getSourceType());
        }
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            existing.setStatus(dto.getStatus());
        }
        repository.updateById(existing);
        saveSnapshotVersion(existing, "UPDATE");
        writeAudit("KNOWLEDGE_ITEM", existing.getId(), "UPDATE", oldValue, snapshotJson(existing));
        return mapper.poToVo(existing);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        KnowledgeItemPO existing = repository.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "Knowledge item not found");
        }
        String oldValue = snapshotJson(existing);
        saveSnapshotVersion(existing, "DELETE_SNAPSHOT");
        repository.deleteById(id);
        writeAudit("KNOWLEDGE_ITEM", id, "DELETE", oldValue, null);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KnowledgeItemVO> search(Long courseId, String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            IPage<KnowledgeItemPO> poPage = repository.findByCourseId(
                    MybatisPlusPageUtils.toMpPage(pageable), courseId);
            return mapper.poPageToVoPage(MybatisPlusPageUtils.toSpringPage(poPage, pageable));
        }
        IPage<KnowledgeItemPO> poPage = repository.findByCourseIdAndTitleContainingIgnoreCase(
                MybatisPlusPageUtils.toMpPage(pageable), courseId, keyword.trim());
        return mapper.poPageToVoPage(MybatisPlusPageUtils.toSpringPage(poPage, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KnowledgeItemVO> searchAdvanced(Long courseId, String keyword, List<String> tags, String tagMode,
                                                Pageable pageable) {
        List<KnowledgeItemPO> candidates = repository.searchByCourseAndKeyword(courseId,
                keyword == null ? null : keyword.trim());
        List<String> normalizedTags = normalizeTags(tags);
        if (!normalizedTags.isEmpty()) {
            boolean andMode = "AND".equalsIgnoreCase(tagMode);
            candidates = candidates.stream()
                    .filter(item -> matchesTags(item.getTags(), normalizedTags, andMode))
                    .toList();
        }
        return toPage(candidates, pageable).map(mapper::poToVo);
    }

    @Override
    @Transactional
    public KnowledgeAnalyzeResultVO autoAnalyze(Long courseId, KnowledgeAnalyzeDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        List<KnowledgeItemPO> created = new ArrayList<>();
        String normalizedTags = normalizeCsvTags(dto.getTags());
        String category = defaultIfBlank(dto.getCategory(), "Auto Parsed");

        List<String> chunks = splitLongText(dto.getText());
        int splitCount = 0;
        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);
            if (chunk.length() < 30) continue;
            KnowledgeItemPO part = KnowledgeItemPO.builder()
                    .courseId(courseId)
                    .title(dto.getTitle() + " - Part " + (i + 1))
                    .description(shorten(chunk, 400))
                    .content(chunk)
                    .fileType(defaultIfBlank(dto.getFileType(), "DOCUMENT"))
                    .fileUrl(dto.getFileUrl())
                    .category(category)
                    .tags(normalizedTags)
                    .extractedText(chunk)
                    .sourceType("AUTO_SPLIT")
                    .status("PUBLISHED")
                    .createdBy(currentUser.getId())
                    .build();
            repository.insert(part);
            saveSnapshotVersion(part, "AUTO_ANALYZE_SPLIT");
            writeAudit("KNOWLEDGE_ITEM", part.getId(), "AUTO_ANALYZE_CREATE", null, snapshotJson(part));
            created.add(part);
            splitCount++;
        }

        List<String> keyPoints = extractBulletLines(dto.getText(), 8, "重点", "核心", "关键", "总结");
        List<String> difficultPoints = extractBulletLines(dto.getText(), 6, "难点", "易错", "注意", "陷阱");
        List<String> examPoints = extractBulletLines(dto.getText(), 6, "考点", "题型", "考试", "exam", "quiz");

        KnowledgeItemPO coreSummary = saveSummary(courseId, currentUser.getId(),
                "核心知识点", dto.getTitle(), keyPoints, category, normalizedTags);
        KnowledgeItemPO difficultSummary = saveSummary(courseId, currentUser.getId(),
                "重难点", dto.getTitle(), difficultPoints, category, normalizedTags);
        KnowledgeItemPO examSummary = saveSummary(courseId, currentUser.getId(),
                "高频考点", dto.getTitle(), examPoints, category, normalizedTags);

        List<KnowledgeItemPO> summaryItems = List.of(coreSummary, difficultSummary, examSummary);
        created.addAll(summaryItems);
        for (KnowledgeItemPO summary : summaryItems) {
            saveSnapshotVersion(summary, "AUTO_ANALYZE_SUMMARY");
            writeAudit("KNOWLEDGE_ITEM", summary.getId(), "AUTO_ANALYZE_CREATE", null, snapshotJson(summary));
        }

        // Link summaries with parsed chunks to form a lightweight graph.
        for (KnowledgeItemPO summary : summaryItems) {
            for (int i = 0; i < Math.min(3, splitCount); i++) {
                KnowledgeItemPO target = created.get(i);
                if (summary.getId().equals(target.getId())) continue;
                if (!associationRepository.existsBySourceIdAndTargetId(summary.getId(), target.getId())) {
                    associationRepository.insert(KnowledgeAssociationPO.builder()
                            .sourceId(summary.getId())
                            .targetId(target.getId())
                            .relationType("DERIVED_FROM")
                            .description("Auto generated from long document analysis")
                            .createdBy(currentUser.getId())
                            .build());
                }
            }
        }

        log.info("Auto analyzed long text for course {}: {} items created", courseId, created.size());
        return KnowledgeAnalyzeResultVO.builder()
                .totalCreated(created.size())
                .splitParts(splitCount)
                .extractedSummaries(summaryItems.size())
                .items(created.stream().map(mapper::poToVo).toList())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public KnowledgeGraphVO buildGraph(Long courseId) {
        List<KnowledgeItemPO> items = repository.findByCourseId(courseId);
        if (items.isEmpty()) {
            return KnowledgeGraphVO.builder()
                    .nodes(Collections.emptyList())
                    .edges(Collections.emptyList())
                    .totalNodes(0)
                    .totalEdges(0)
                    .build();
        }
        List<Long> ids = items.stream().map(KnowledgeItemPO::getId).toList();
        List<KnowledgeAssociationPO> associations = associationRepository.findBySourceIdInOrTargetIdIn(ids, ids);

        List<KnowledgeGraphVO.GraphNodeVO> nodes = items.stream()
                .map(i -> KnowledgeGraphVO.GraphNodeVO.builder()
                        .id(i.getId())
                        .label(i.getTitle())
                        .category(defaultIfBlank(i.getCategory(), "General"))
                        .description(i.getDescription())
                        .properties(java.util.Map.of(
                                "tags", defaultIfBlank(i.getTags(), ""),
                                "status", defaultIfBlank(i.getStatus(), "PUBLISHED"),
                                "sourceType", defaultIfBlank(i.getSourceType(), "MANUAL")
                        ))
                        .build())
                .toList();
        List<KnowledgeGraphVO.GraphEdgeVO> edges = associations.stream()
                .filter(a -> ids.contains(a.getSourceId()) && ids.contains(a.getTargetId()))
                .map(a -> KnowledgeGraphVO.GraphEdgeVO.builder()
                        .id(a.getId())
                        .source(a.getSourceId())
                        .target(a.getTargetId())
                        .relationType(a.getRelationType())
                        .description(a.getDescription())
                        .build())
                .toList();

        return KnowledgeGraphVO.builder()
                .nodes(nodes)
                .edges(edges)
                .totalNodes(nodes.size())
                .totalEdges(edges.size())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public String generateMindMap(Long courseId, Long itemId) {
        KnowledgeItemPO root = repository.selectById(itemId);
        if (root == null) {
            throw new BusinessException(404, "Knowledge item not found");
        }
        if (!root.getCourseId().equals(courseId)) {
            throw new BusinessException(403, "Knowledge item does not belong to this course");
        }
        List<KnowledgeAssociationPO> outgoing = associationRepository.findBySourceId(itemId);
        List<KnowledgeAssociationPO> incoming = associationRepository.findByTargetId(itemId);

        StringBuilder sb = new StringBuilder();
        sb.append("mindmap\n");
        sb.append("  root((").append(sanitizeMindmapText(root.getTitle())).append("))\n");

        if (!outgoing.isEmpty()) {
            sb.append("    关联知识点\n");
            for (KnowledgeAssociationPO edge : outgoing.stream().limit(8).toList()) {
                KnowledgeItemPO target = repository.selectById(edge.getTargetId());
                String title = target != null ? target.getTitle() : "Unknown";
                sb.append("      ").append(sanitizeMindmapText(title))
                        .append(" (").append(defaultIfBlank(edge.getRelationType(), "RELATED")).append(")\n");
            }
        }
        if (!incoming.isEmpty()) {
            sb.append("    来源知识点\n");
            for (KnowledgeAssociationPO edge : incoming.stream().limit(8).toList()) {
                KnowledgeItemPO source = repository.selectById(edge.getSourceId());
                String title = source != null ? source.getTitle() : "Unknown";
                sb.append("      ").append(sanitizeMindmapText(title))
                        .append(" (").append(defaultIfBlank(edge.getRelationType(), "RELATED")).append(")\n");
            }
        }
        return sb.toString();
    }

    @Override
    @Transactional
    public KnowledgeResourceVO addResource(Long courseId, Long itemId, KnowledgeResourceDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        KnowledgeItemPO item = repository.selectById(itemId);
        if (item == null) {
            throw new BusinessException(404, "Knowledge item not found");
        }
        if (!item.getCourseId().equals(courseId)) {
            throw new BusinessException(403, "Knowledge item does not belong to this course");
        }
        KnowledgeResourcePO po = KnowledgeResourcePO.builder()
                .knowledgeItemId(itemId)
                .resourceType(dto.getResourceType().toUpperCase(Locale.ROOT))
                .title(dto.getTitle())
                .url(dto.getUrl())
                .description(dto.getDescription())
                .createdBy(currentUser.getId())
                .build();
        knowledgeResourceRepository.insert(po);
        writeAudit("KNOWLEDGE_ITEM_RESOURCE", itemId, "RESOURCE_ATTACH", null, resourceJson(po));
        return toResourceVO(po);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KnowledgeResourceVO> listResources(Long courseId, Long itemId) {
        KnowledgeItemPO item = repository.selectById(itemId);
        if (item == null) {
            throw new BusinessException(404, "Knowledge item not found");
        }
        if (!item.getCourseId().equals(courseId)) {
            throw new BusinessException(403, "Knowledge item does not belong to this course");
        }
        return knowledgeResourceRepository.findByKnowledgeItemIdOrderByCreatedAtDesc(itemId).stream()
                .map(this::toResourceVO)
                .toList();
    }

    @Override
    @Transactional
    public void deleteResource(Long courseId, Long itemId, Long resourceId) {
        KnowledgeItemPO item = repository.selectById(itemId);
        if (item == null) {
            throw new BusinessException(404, "Knowledge item not found");
        }
        if (!item.getCourseId().equals(courseId)) {
            throw new BusinessException(403, "Knowledge item does not belong to this course");
        }
        KnowledgeResourcePO po = knowledgeResourceRepository.selectById(resourceId);
        if (po == null) {
            throw new BusinessException(404, "Knowledge resource not found");
        }
        if (!po.getKnowledgeItemId().equals(itemId)) {
            throw new BusinessException(400, "Resource does not belong to target knowledge item");
        }
        String oldValue = resourceJson(po);
        knowledgeResourceRepository.deleteById(po.getId());
        writeAudit("KNOWLEDGE_ITEM_RESOURCE", itemId, "RESOURCE_DELETE", oldValue, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VersionPO> listVersions(Long courseId, Long itemId) {
        KnowledgeItemPO item = repository.selectById(itemId);
        if (item == null) {
            throw new BusinessException(404, "Knowledge item not found");
        }
        if (!item.getCourseId().equals(courseId)) {
            throw new BusinessException(403, "Knowledge item does not belong to this course");
        }
        return versionService.listVersions("KNOWLEDGE_ITEM", itemId);
    }

    @Override
    @Transactional
    public KnowledgeItemVO rollbackToVersion(Long courseId, Long itemId, Integer versionNumber) {
        KnowledgeItemPO item = repository.selectById(itemId);
        if (item == null) {
            throw new BusinessException(404, "Knowledge item not found");
        }
        if (!item.getCourseId().equals(courseId)) {
            throw new BusinessException(403, "Knowledge item does not belong to this course");
        }
        VersionPO target = versionService.getVersion("KNOWLEDGE_ITEM", itemId, versionNumber);
        String oldValue = snapshotJson(item);
        applySnapshot(item, target.getContent());
        repository.updateById(item);
        saveSnapshotVersion(item, "ROLLBACK_TO_V" + versionNumber);
        writeAudit("KNOWLEDGE_ITEM", itemId, "ROLLBACK", oldValue, snapshotJson(item));
        return mapper.poToVo(item);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogPO> listAuditLogs(Long courseId, Long itemId, Pageable pageable) {
        KnowledgeItemPO item = repository.selectById(itemId);
        if (item == null) {
            throw new BusinessException(404, "Knowledge item not found");
        }
        if (!item.getCourseId().equals(courseId)) {
            throw new BusinessException(403, "Knowledge item does not belong to this course");
        }
        IPage<AuditLogPO> merged = auditLogRepository.findByEntityTypesAndEntityId(
                MybatisPlusPageUtils.toMpPage(pageable),
                List.of("KNOWLEDGE_ITEM", "KNOWLEDGE_ITEM_RESOURCE"),
                itemId);
        return MybatisPlusPageUtils.toSpringPage(merged, pageable);
    }

    private void saveSnapshotVersion(KnowledgeItemPO item, String description) {
        versionService.saveVersion("KNOWLEDGE_ITEM", item.getId(), snapshotJson(item), description);
    }

    private String snapshotJson(KnowledgeItemPO item) {
        try {
            KnowledgeItemSnapshot snapshot = KnowledgeItemSnapshot.builder()
                    .title(item.getTitle())
                    .description(item.getDescription())
                    .content(item.getContent())
                    .fileUrl(item.getFileUrl())
                    .fileType(item.getFileType())
                    .category(item.getCategory())
                    .tags(item.getTags())
                    .extractedText(item.getExtractedText())
                    .sourceType(item.getSourceType())
                    .status(item.getStatus())
                    .build();
            return objectMapper.writeValueAsString(snapshot);
        } catch (Exception e) {
            throw new BusinessException(500, "Failed to serialize knowledge snapshot");
        }
    }

    private void applySnapshot(KnowledgeItemPO item, String content) {
        try {
            KnowledgeItemSnapshot snapshot = objectMapper.readValue(content, KnowledgeItemSnapshot.class);
            item.setTitle(snapshot.getTitle());
            item.setDescription(snapshot.getDescription());
            item.setContent(snapshot.getContent());
            item.setFileUrl(snapshot.getFileUrl());
            item.setFileType(snapshot.getFileType());
            item.setCategory(snapshot.getCategory());
            item.setTags(snapshot.getTags());
            item.setExtractedText(snapshot.getExtractedText());
            if (snapshot.getSourceType() != null && !snapshot.getSourceType().isBlank()) {
                item.setSourceType(snapshot.getSourceType());
            }
            if (snapshot.getStatus() != null && !snapshot.getStatus().isBlank()) {
                item.setStatus(snapshot.getStatus());
            }
        } catch (Exception e) {
            throw new BusinessException(500, "Failed to parse knowledge snapshot");
        }
    }

    private String resourceJson(KnowledgeResourcePO po) {
        try {
            return objectMapper.writeValueAsString(java.util.Map.of(
                    "resourceId", po.getId(),
                    "knowledgeItemId", po.getKnowledgeItemId(),
                    "resourceType", defaultIfBlank(po.getResourceType(), ""),
                    "title", defaultIfBlank(po.getTitle(), ""),
                    "url", defaultIfBlank(po.getUrl(), ""),
                    "description", defaultIfBlank(po.getDescription(), "")
            ));
        } catch (Exception e) {
            return "{}";
        }
    }

    private void writeAudit(String entityType, Long entityId, String action, String oldValue, String newValue) {
        Long operatorId = null;
        String operatorName = "SYSTEM";
        try {
            User current = SecurityUtils.getCurrentUser();
            operatorId = current.getId();
            operatorName = current.getUsername();
        } catch (Exception ignored) {
        }
        auditLogRepository.insert(AuditLogPO.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .oldValue(oldValue)
                .newValue(newValue)
                .operatorId(operatorId)
                .operatorName(operatorName)
                .build());
    }

    private KnowledgeItemPO saveSummary(Long courseId, Long userId, String prefix, String title,
                                        List<String> bullets, String category, String tags) {
        String normalized = bullets.isEmpty()
                ? "- 暂无自动提取结果，请补充文档内容"
                : bullets.stream().map(s -> "- " + s).collect(Collectors.joining("\n"));
        KnowledgeItemPO summary = KnowledgeItemPO.builder()
                .courseId(courseId)
                .title(prefix + " - " + title)
                .description(normalized)
                .content(normalized)
                .category(category)
                .tags(tags)
                .sourceType("AUTO_SUMMARY")
                .status("PUBLISHED")
                .createdBy(userId)
                .build();
        repository.insert(summary);
        return summary;
    }

    private List<String> splitLongText(String text) {
        if (text == null || text.isBlank()) return Collections.emptyList();
        String[] raw = SPLIT_PATTERN.split(text);
        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String piece : raw) {
            String normalized = piece == null ? "" : piece.trim();
            if (normalized.isEmpty()) continue;
            if (current.length() + normalized.length() > 420) {
                chunks.add(current.toString().trim());
                current = new StringBuilder();
            }
            if (!current.isEmpty()) current.append(" ");
            current.append(normalized);
        }
        if (!current.isEmpty()) chunks.add(current.toString().trim());
        return chunks.stream().filter(s -> !s.isBlank()).toList();
    }

    private List<String> extractBulletLines(String text, int limit, String... hints) {
        List<String> lines = splitLongText(text).stream().limit(64).toList();
        List<String> preferred = new ArrayList<>();
        for (String line : lines) {
            String lower = line.toLowerCase(Locale.ROOT);
            for (String hint : hints) {
                if (lower.contains(hint.toLowerCase(Locale.ROOT))) {
                    preferred.add(shorten(line, 120));
                    break;
                }
            }
            if (preferred.size() >= limit) break;
        }
        if (preferred.size() < limit) {
            for (String line : lines) {
                String v = shorten(line, 120);
                if (!preferred.contains(v)) preferred.add(v);
                if (preferred.size() >= limit) break;
            }
        }
        return preferred;
    }

    private boolean matchesTags(String itemTags, List<String> requiredTags, boolean andMode) {
        Set<String> normalized = parseCsvTags(itemTags);
        if (normalized.isEmpty()) return false;
        if (andMode) {
            return requiredTags.stream().allMatch(normalized::contains);
        }
        return requiredTags.stream().anyMatch(normalized::contains);
    }

    private List<String> normalizeTags(List<String> tags) {
        if (tags == null) return Collections.emptyList();
        return tags.stream()
                .filter(t -> t != null && !t.isBlank())
                .map(t -> t.trim().toLowerCase(Locale.ROOT))
                .distinct()
                .toList();
    }

    private Set<String> parseCsvTags(String csv) {
        if (csv == null || csv.isBlank()) return Collections.emptySet();
        return java.util.Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String normalizeCsvTags(String csv) {
        if (csv == null || csv.isBlank()) return "";
        return java.util.Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .collect(Collectors.joining(","));
    }

    private <T> Page<T> toPage(List<T> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), list.size());
        if (start >= list.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, list.size());
        }
        return new PageImpl<>(list.subList(start, end), pageable, list.size());
    }

    private String shorten(String text, int maxLen) {
        if (text == null) return "";
        String normalized = text.trim().replaceAll("\\s+", " ");
        if (normalized.length() <= maxLen) return normalized;
        return normalized.substring(0, maxLen).trim() + "...";
    }

    private String defaultIfBlank(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private String sanitizeMindmapText(String text) {
        return defaultIfBlank(text, "Untitled")
                .replace("\n", " ")
                .replace("(", " ")
                .replace(")", " ")
                .replace("\"", "'");
    }

    private KnowledgeResourceVO toResourceVO(KnowledgeResourcePO po) {
        return KnowledgeResourceVO.builder()
                .id(po.getId())
                .knowledgeItemId(po.getKnowledgeItemId())
                .resourceType(po.getResourceType())
                .title(po.getTitle())
                .url(po.getUrl())
                .description(po.getDescription())
                .createdBy(po.getCreatedBy())
                .createdAt(po.getCreatedAt())
                .build();
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class KnowledgeItemSnapshot {
        private String title;
        private String description;
        private String content;
        private String fileUrl;
        private String fileType;
        private String category;
        private String tags;
        private String extractedText;
        private String sourceType;
        private String status;
    }

}
