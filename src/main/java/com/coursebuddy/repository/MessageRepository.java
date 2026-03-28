package com.coursebuddy.repository;

import com.coursebuddy.domain.po.MessagePO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<MessagePO, Long> {

    @Query("SELECT m FROM MessagePO m WHERE (m.senderId = :userId1 AND m.receiverId = :userId2) OR (m.senderId = :userId2 AND m.receiverId = :userId1) ORDER BY m.createdAt DESC")
    Page<MessagePO> findConversation(Long userId1, Long userId2, Pageable pageable);

    Page<MessagePO> findByReceiverIdOrderByCreatedAtDesc(Long receiverId, Pageable pageable);

    long countByReceiverIdAndIsRead(Long receiverId, Boolean isRead);

    @Modifying
    @Query("UPDATE MessagePO m SET m.isRead = true WHERE m.senderId = :senderId AND m.receiverId = :receiverId AND m.isRead = false")
    int markConversationRead(Long senderId, Long receiverId);
}
