package com.coursebuddy.repository;

import com.coursebuddy.domain.po.FileUploadPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileUploadRepository extends JpaRepository<FileUploadPO, Long> {
    Optional<FileUploadPO> findByObjectNameAndIsDeletedFalse(String objectName);
}
