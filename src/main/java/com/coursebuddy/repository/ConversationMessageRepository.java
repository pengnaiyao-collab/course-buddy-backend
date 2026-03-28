package com.coursebuddy.repository;

import com.coursebuddy.domain.po.ConversationMessagePO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationMessageRepository extends JpaRepository<ConversationMessagePO, Long> {

    List<ConversationMessagePO> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

    @Modifying
    @Query("DELETE FROM ConversationMessagePO m WHERE m.conversationId = :conversationId")
    void deleteByConversationId(@Param("conversationId") Long conversationId);
}
