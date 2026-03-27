package com.coursebuddy.repository;

import com.coursebuddy.domain.po.VersionPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VersionRepository extends JpaRepository<VersionPO, Long> {

    List<VersionPO> findByEntityTypeAndEntityIdOrderByVersionNumberDesc(
            String entityType, Long entityId);

    Optional<VersionPO> findByEntityTypeAndEntityIdAndVersionNumber(
            String entityType, Long entityId, int versionNumber);

    @Query("SELECT COALESCE(MAX(v.versionNumber), 0) FROM VersionPO v " +
            "WHERE v.entityType = :entityType AND v.entityId = :entityId")
    int findMaxVersionNumber(String entityType, Long entityId);
}
