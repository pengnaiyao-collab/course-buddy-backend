package com.coursebuddy.notes;

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
public class NoteService {

    private final NoteRepository noteRepository;

    @Transactional
    public Note create(Note note) {
        User currentUser = getCurrentUser();
        note.setUserId(currentUser.getId());
        return noteRepository.save(note);
    }

    @Transactional(readOnly = true)
    public Page<Note> listMyNotes(Long courseId, Pageable pageable) {
        User currentUser = getCurrentUser();
        if (courseId != null) {
            return noteRepository.findByUserIdAndCourseId(currentUser.getId(), courseId, pageable);
        }
        return noteRepository.findByUserId(currentUser.getId(), pageable);
    }

    @Transactional(readOnly = true)
    public Note getById(Long id) {
        User currentUser = getCurrentUser();
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Note not found"));
        if (!note.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(403, "You are not authorized to view this note");
        }
        return note;
    }

    @Transactional
    public Note update(Long id, Note item) {
        User currentUser = getCurrentUser();
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Note not found"));
        if (!note.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(403, "You are not authorized to update this note");
        }
        note.setTitle(item.getTitle());
        note.setContent(item.getContent());
        if (item.getCategory() != null) note.setCategory(item.getCategory());
        if (item.getTags() != null) note.setTags(item.getTags());
        return noteRepository.save(note);
    }

    @Transactional
    public void delete(Long id) {
        User currentUser = getCurrentUser();
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Note not found"));
        if (!note.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(403, "You are not authorized to delete this note");
        }
        noteRepository.delete(note);
    }

    @Transactional(readOnly = true)
    public Page<Note> search(String keyword, Pageable pageable) {
        User currentUser = getCurrentUser();
        return noteRepository.findByUserIdAndTitleContainingIgnoreCase(currentUser.getId(), keyword, pageable);
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User user) {
            return user;
        }
        throw new BusinessException(401, "User not authenticated");
    }
}
