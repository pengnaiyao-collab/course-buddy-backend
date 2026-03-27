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
    public KnowledgeItem create(Long courseId, KnowledgeItem item) {
        User currentUser = getCurrentUser();
        item.setCourseId(courseId);
        item.setCreatedBy(currentUser.getId());
        return repository.save(item);
    }

    @Transactional(readOnly = true)
    public Page<KnowledgeItem> listByCourse(Long courseId, Pageable pageable) {
        return repository.findByCourseId(courseId, pageable);
    }

    @Transactional(readOnly = true)
    public KnowledgeItem getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Knowledge item not found"));
    }

    @Transactional
    public KnowledgeItem update(Long id, KnowledgeItem item) {
        KnowledgeItem existing = repository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Knowledge item not found"));
        existing.setTitle(item.getTitle());
        existing.setDescription(item.getDescription());
        existing.setFileUrl(item.getFileUrl());
        existing.setFileType(item.getFileType());
        existing.setCategory(item.getCategory());
        existing.setTags(item.getTags());
        return repository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new BusinessException(404, "Knowledge item not found");
        }
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<KnowledgeItem> search(Long courseId, String keyword, Pageable pageable) {
        return repository.findByCourseIdAndTitleContainingIgnoreCase(courseId, keyword, pageable);
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User user) {
            return user;
        }
        throw new BusinessException(401, "User not authenticated");
    }
}
