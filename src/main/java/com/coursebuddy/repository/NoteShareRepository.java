package com.coursebuddy.repository;

import com.coursebuddy.domain.po.NoteSharePO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NoteShareRepository extends JpaRepository<NoteSharePO, Long> {

    Optional<NoteSharePO> findByShareToken(String shareToken);

    Page<NoteSharePO> findByOwnerIdOrderByCreatedAtDesc(Long ownerId, Pageable pageable);

    Page<NoteSharePO> findByNoteIdAndOwnerIdOrderByCreatedAtDesc(Long noteId, Long ownerId, Pageable pageable);
}
