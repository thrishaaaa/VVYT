package com.legal_system.mediation.repository;

import com.legal_system.mediation.model.AdvisorMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AdvisorMessageRepository extends JpaRepository<AdvisorMessage, Integer> {

    @Query("SELECT m FROM AdvisorMessage m WHERE (m.senderId = ?1 AND m.legalAdvisor.advisorId = ?2) OR (m.senderId = ?2 AND m.legalAdvisor.advisorId = ?1) ORDER BY m.sentAt")
    List<AdvisorMessage> findConversation(Integer userId, Integer advisorId);

    @Query("SELECT COUNT(m) FROM AdvisorMessage m WHERE m.legalAdvisor.advisorId = ?1 AND m.isRead = false AND m.senderType = 'USER'")
    Long countUnreadMessagesForAdvisor(Integer advisorId);

    @Query("SELECT COUNT(m) FROM AdvisorMessage m WHERE m.senderId = ?1 AND m.isRead = false AND m.senderType = 'ADVISOR'")
    Long countUnreadMessagesForUser(Integer userId);
}
