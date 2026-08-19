package com.iTech.education.repository;

import com.iTech.education.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT m FROM ChatMessage m LEFT JOIN FETCH m.staff LEFT JOIN FETCH m.conversation c LEFT JOIN FETCH c.user WHERE c.id = :conversationId ORDER BY m.createdAt ASC, m.id ASC")
    List<ChatMessage> findByConversationIdOrderByCreatedAtAsc(@Param("conversationId") Long conversationId);
}
