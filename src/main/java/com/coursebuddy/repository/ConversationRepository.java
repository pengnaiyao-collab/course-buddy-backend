package com.coursebuddy.repository;

import com.coursebuddy.domain.po.ConversationPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationRepository extends JpaRepository<ConversationPO, Long> {

    Page<ConversationPO> findByUserIdOrderByUpdatedAtDesc(Long userId, Pageable pageable);

    Page<ConversationPO> findByUserIdAndStatusOrderByUpdatedAtDesc(Long userId, String status, Pageable pageable);
}
