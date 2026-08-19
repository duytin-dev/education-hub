package com.iTech.education.repository;

import com.iTech.education.entity.Conversation;
import com.iTech.education.utils.ConversationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByGuestToken(String guestToken);

    Optional<Conversation> findFirstByUser_IdAndStatusOrderByLastMessageAtDesc(Long userId, ConversationStatus status);

    @Query("SELECT c FROM Conversation c LEFT JOIN FETCH c.user LEFT JOIN FETCH c.course ORDER BY COALESCE(c.lastMessageAt, c.createdAt) DESC")
    List<Conversation> findAllForStaff();

    long countByUnreadForStaffGreaterThan(int value);
}
