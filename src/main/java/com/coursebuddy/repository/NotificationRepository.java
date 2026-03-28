package com.coursebuddy.repository;

import com.coursebuddy.domain.po.NotificationPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationPO, Long> {

    Page<NotificationPO> findByUserId(Long userId, Pageable pageable);

    Page<NotificationPO> findByUserIdAndIsRead(Long userId, Boolean isRead, Pageable pageable);

    Page<NotificationPO> findByUserIdAndIsReadAndType(Long userId, Boolean isRead, String type, Pageable pageable);

    Page<NotificationPO> findByUserIdAndType(Long userId, String type, Pageable pageable);

    long countByUserIdAndIsRead(Long userId, Boolean isRead);

    @Modifying
    @Query("UPDATE NotificationPO n SET n.isRead = true WHERE n.userId = :userId")
    int markAllReadByUserId(Long userId);
}
