package com.coursebuddy.repository;

import com.coursebuddy.domain.po.OcrResultPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OcrResultRepository extends JpaRepository<OcrResultPO, Long> {
    Optional<OcrResultPO> findByObjectName(String objectName);
    List<OcrResultPO> findByFileUploadId(Long fileUploadId);
    List<OcrResultPO> findByStatus(String status);
}
