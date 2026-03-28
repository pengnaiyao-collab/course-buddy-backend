package com.coursebuddy.repository;

import com.coursebuddy.domain.po.NoteTagPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteTagRepository extends JpaRepository<NoteTagPO, Long> {

    List<NoteTagPO> findByUserIdOrderByUseCountDesc(Long userId);

    Optional<NoteTagPO> findByIdAndUserId(Long id, Long userId);

    Optional<NoteTagPO> findByUserIdAndName(Long userId, String name);

    List<NoteTagPO> findByUserIdAndNameContainingIgnoreCase(Long userId, String keyword);
}
