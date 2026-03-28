package com.coursebuddy.repository;

import com.coursebuddy.domain.po.KnowledgeAssociationPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KnowledgeAssociationRepository extends JpaRepository<KnowledgeAssociationPO, Long> {
    List<KnowledgeAssociationPO> findBySourceId(Long sourceId);
    List<KnowledgeAssociationPO> findByTargetId(Long targetId);
    Optional<KnowledgeAssociationPO> findBySourceIdAndTargetId(Long sourceId, Long targetId);
    void deleteBySourceIdAndTargetId(Long sourceId, Long targetId);
    boolean existsBySourceIdAndTargetId(Long sourceId, Long targetId);
}
