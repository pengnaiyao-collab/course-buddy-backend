package com.coursebuddy.repository;

import com.coursebuddy.domain.po.NoteVersionPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteVersionRepository extends JpaRepository<NoteVersionPO, Long> {

    List<NoteVersionPO> findByNoteIdOrderByVersionNoDesc(Long noteId);

    Optional<NoteVersionPO> findByNoteIdAndVersionNo(Long noteId, Integer versionNo);

    @Query("SELECT COALESCE(MAX(nv.versionNo), 0) FROM NoteVersionPO nv WHERE nv.noteId = :noteId")
    Integer getMaxVersionNo(Long noteId);
}
