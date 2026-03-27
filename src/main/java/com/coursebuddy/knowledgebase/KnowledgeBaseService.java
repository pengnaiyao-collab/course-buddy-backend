package com.coursebuddy.knowledgebase;

import com.coursebuddy.auth.User;
import com.coursebuddy.common.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KnowledgeBaseService {

    private final KnowledgeItemRepository repository;

    @Transactional
    public KnowledgeItemResponse create(Long courseId, KnowledgeItemRequest request) {
        User currentUser = getCurrentUser();
        KnowledgeItem item = KnowledgeItem.builder()
                .courseId(courseId)
                .title(request.getTitle())
                .description(request.getDescription())
                .fileUrl(request.getFileUrl())
                .fileType(request.getFileType())
                .category(request.getCategory())
                .tags(request.getTags())
                .createdBy(currentUser.getId())
                .build();
        return KnowledgeItemResponse.from(repository.save(item));
    }

    @Transactional(readOnly = true)
    public Page<KnowledgeItemResponse> listByCourse(Long courseId, Pageable pageable) {
        return repository.findByCourseId(courseId, pageable).map(KnowledgeItemResponse::from);
    }

    @Transactional(readOnly = true)
    public KnowledgeItemResponse getById(Long id) {
        return KnowledgeItemResponse.from(repository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Knowledge item not found")));
    }

    @Transactional
    public KnowledgeItemResponse update(Long id, KnowledgeItemRequest request) {
        KnowledgeItem item = repository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Knowledge item not found"));
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setFileUrl(request.getFileUrl());
        item.setFileType(request.getFileType());
        item.setCategory(request.getCategory());
        item.setTags(request.getTags());
        return KnowledgeItemResponse.from(repository.save(item));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new BusinessException(404, "Knowledge item not found");
        }
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<KnowledgeItemResponse> search(Long courseId, String keyword, Pageable pageable) {
        return repository.findByCourseIdAndTitleContainingIgnoreCase(courseId, keyword, pageable)
                .map(KnowledgeItemResponse::from);
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User user) {
            return user;
        }
        throw new BusinessException(401, "User not authenticated");
    }
}
