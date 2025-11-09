package com.legal_system.mediation.Service;

import com.legal_system.mediation.model.LegalAdvisor;
import com.legal_system.mediation.model.AdvisorMessage;
import com.legal_system.mediation.repository.LegalAdvisorRepository;
import com.legal_system.mediation.repository.AdvisorMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LegalAdvisorService {

    @Autowired
    private LegalAdvisorRepository advisorRepository;

    @Autowired
    private AdvisorMessageRepository messageRepository;

    /**
     * Get all legal advisors
     */
    public List<LegalAdvisor> getAllAdvisors() {
        return advisorRepository.findAll();
    }

    /**
     * Get all available advisors
     */
    public List<LegalAdvisor> getAvailableAdvisors() {
        return advisorRepository.findByAvailabilityStatusTrue();
    }

    /**
     * Get top rated advisors
     */
    public List<LegalAdvisor> getTopRatedAdvisors() {
        return advisorRepository.findTopRatedAdvisors();
    }

    /**
     * Get advisor by ID
     */
    public LegalAdvisor getAdvisorById(Integer advisorId) {
        return advisorRepository.findById(advisorId)
                .orElseThrow(() -> new RuntimeException("Legal Advisor not found with ID: " + advisorId));
    }

    /**
     * Search advisors by name
     */
    public List<LegalAdvisor> searchAdvisorsByName(String name) {
        return advisorRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Filter advisors by university
     */
    public List<LegalAdvisor> getAdvisorsByUniversity(String university) {
        return advisorRepository.findByUniversity(university);
    }

    /**
     * Filter advisors by year of study
     */
    public List<LegalAdvisor> getAdvisorsByYear(String year) {
        return advisorRepository.findByYearOfStudy(year);
    }

    /**
     * Save or update advisor (Admin only)
     */
    public LegalAdvisor saveAdvisor(LegalAdvisor advisor) {
        return advisorRepository.save(advisor);
    }

    /**
     * Delete advisor (Admin only)
     */
    public void deleteAdvisor(Integer advisorId) {
        advisorRepository.deleteById(advisorId);
    }

    /**
     * Send message to advisor
     */
    public AdvisorMessage sendMessage(Integer senderId, Integer advisorId, String messageText, String senderType) {
        AdvisorMessage message = new AdvisorMessage();
        message.setSenderId(senderId);

        // Instead of message.setAdvisorId(advisorId), fetch LegalAdvisor and set it
        LegalAdvisor advisor = advisorRepository.findById(advisorId)
                .orElseThrow(() -> new RuntimeException("Advisor not found"));
        message.setLegalAdvisor(advisor);

        message.setMessage(messageText);
        message.setSenderType(senderType);
        return messageRepository.save(message);
    }


    /**
     * Get conversation between user and advisor
     */
    public List<AdvisorMessage> getConversation(Integer userId, Integer advisorId) {
        return messageRepository.findConversation(userId, advisorId);
    }

    /**
     * Mark messages as read
     */
    public void markMessagesAsRead(List<Integer> messageIds) {
        for (Integer messageId : messageIds) {
            messageRepository.findById(messageId).ifPresent(msg -> {
                msg.setIsRead(true);
                messageRepository.save(msg);
            });
        }
    }

    /**
     * Get unread message count for advisor
     */
    public Long getUnreadCountForAdvisor(Integer advisorId) {
        return messageRepository.countUnreadMessagesForAdvisor(advisorId);
    }

    /**
     * Get unread message count for user
     */
    public Long getUnreadCountForUser(Integer userId) {
        return messageRepository.countUnreadMessagesForUser(userId);
    }
}
