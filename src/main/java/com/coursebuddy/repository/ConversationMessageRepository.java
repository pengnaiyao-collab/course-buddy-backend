package com.coursebuddy.repository;

import com.coursebuddy.domain.po.ConversationMessagePO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationMessageRepository extends JpaRepository<ConversationMessagePO, Long> {

    List<ConversationMessagePO> findByConversationIdOrderByCreatedAtAsc(Long conversationId);
}
