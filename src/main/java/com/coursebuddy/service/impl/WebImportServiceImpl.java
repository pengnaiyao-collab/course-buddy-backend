package com.coursebuddy.service.impl;

import com.coursebuddy.auth.User;
import com.coursebuddy.common.MybatisPlusPageUtils;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.coursebuddy.domain.dto.KnowledgeItemDTO;
import com.coursebuddy.domain.dto.WebImportDTO;
import com.coursebuddy.domain.po.WebImportPO;
import com.coursebuddy.domain.vo.KnowledgeItemVO;
import com.coursebuddy.domain.vo.WebImportVO;
import com.coursebuddy.mapper.WebImportMapper;
import com.coursebuddy.service.IKnowledgeBaseService;
import com.coursebuddy.service.IWebImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebImportServiceImpl implements IWebImportService {

    private final WebImportMapper webImportRepository;
    private final IKnowledgeBaseService knowledgeBaseService;

    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int MAX_BODY_SIZE = 5 * 1024 * 1024; // 5 MB

    @Override
    @Transactional
    public WebImportVO importWebPage(Long courseId, WebImportDTO dto) {
        validateUrl(dto.getUrl());

        Long userId = null;
        try {
            User user = SecurityUtils.getCurrentUser();
            userId = user.getId();
        } catch (Exception ignored) {}

        WebImportPO po = WebImportPO.builder()
                .courseId(courseId)
                .url(dto.getUrl())
                .status("PENDING")
                .createdBy(userId)
                .build();
        webImportRepository.insert(po);

        try {
            Document doc = Jsoup.connect(dto.getUrl())
                    .timeout(CONNECT_TIMEOUT_MS)
                    .maxBodySize(MAX_BODY_SIZE)
                    .userAgent("CourseBuddy/1.0 (+https://coursebuddy.example.com)")
                    .get();

            String title = doc.title();
            String bodyText = Jsoup.clean(doc.body().html(), Safelist.none());
            // Remove blank lines and normalize whitespace
            bodyText = bodyText.replaceAll("(?m)^[ \\t]*\\r?\\n", "")
                    .replaceAll("[ \\t]+", " ")
                    .trim();

            po.setTitle(title);
            po.setContent(bodyText);
            po.setHtmlContent(doc.outerHtml());
            po.setStatus("COMPLETED");

            if (dto.isCreateKnowledgeItem()) {
                String description = bodyText.isBlank() ? dto.getUrl()
                        : (bodyText.length() > 500 ? bodyText.substring(0, 500).trim() + "..." : bodyText);
                KnowledgeItemDTO kid = KnowledgeItemDTO.builder()
                        .title(title != null && !title.isBlank() ? title : dto.getUrl())
                        .description(description)
                        .fileUrl(dto.getUrl())
                        .fileType("WEB_PAGE")
                        .category(dto.getCategory())
                        .tags(dto.getTags())
                        .build();
                KnowledgeItemVO ki = knowledgeBaseService.createForCourse(courseId, kid);
                po.setKnowledgeItemId(ki.getId());
            }

            log.info("Web import completed for URL: {}, course: {}", dto.getUrl(), courseId);
        } catch (IOException e) {
            log.error("Failed to fetch URL {}: {}", dto.getUrl(), e.getMessage());
            po.setStatus("FAILED");
            po.setErrorMessage("无法访问URL: " + e.getMessage());
        } catch (Exception e) {
            log.error("Web import failed for URL {}: {}", dto.getUrl(), e.getMessage(), e);
            po.setStatus("FAILED");
            po.setErrorMessage(e.getMessage());
        }

        webImportRepository.updateById(po);
        WebImportPO saved = po;
        return toVO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WebImportVO> listByCourse(Long courseId, Pageable pageable) {
        IPage<WebImportPO> poPage = webImportRepository.findByCourseId(
                MybatisPlusPageUtils.toMpPage(pageable), courseId);
        return MybatisPlusPageUtils.toSpringPage(poPage, pageable).map(this::toVO);
    }

    @Override
    @Transactional(readOnly = true)
    public WebImportVO getById(Long id) {
        WebImportPO po = webImportRepository.selectById(id);
        if (po == null) {
            throw new BusinessException(404, "Web import record not found");
        }
        return toVO(po);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (webImportRepository.selectById(id) == null) {
            throw new BusinessException(404, "Web import record not found");
        }
        webImportRepository.deleteById(id);
    }

    private WebImportVO toVO(WebImportPO po) {
        return WebImportVO.builder()
                .id(po.getId())
                .courseId(po.getCourseId())
                .url(po.getUrl())
                .title(po.getTitle())
                .content(po.getContent())
                .status(po.getStatus())
                .errorMessage(po.getErrorMessage())
                .knowledgeItemId(po.getKnowledgeItemId())
                .createdBy(po.getCreatedBy())
                .createdAt(po.getCreatedAt())
                .build();
    }

    private void validateUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new BusinessException("URL不能为空");
        }
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                throw new BusinessException("只支持HTTP和HTTPS协议的URL");
            }
            String host = uri.getHost();
            if (host == null || host.isBlank()) {
                throw new BusinessException("URL格式无效");
            }
            if (isPrivateOrLoopback(host)) {
                throw new BusinessException("不允许访问内网地址");
            }
        } catch (URISyntaxException e) {
            throw new BusinessException("URL格式无效: " + e.getMessage());
        }
    }

    private boolean isPrivateOrLoopback(String host) {
        String lower = host.toLowerCase();
        // Reject obvious loopback/localhost names
        if (lower.equals("localhost") || lower.equals("::1") || lower.equals("[::1]")) {
            return true;
        }
        // Use InetAddress for accurate private/loopback detection
        try {
            InetAddress addr = InetAddress.getByName(host);
            return addr.isLoopbackAddress()
                    || addr.isSiteLocalAddress()
                    || addr.isLinkLocalAddress()
                    || addr.isAnyLocalAddress();
        } catch (Exception e) {
            // If resolution fails, allow the URL to proceed (the HTTP client will fail anyway)
            return false;
        }
    }
}
