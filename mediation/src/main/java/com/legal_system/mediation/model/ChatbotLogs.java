package com.legal_system.mediation.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chatbotlogs")
public class ChatbotLogs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer chat_id;

    @ManyToOne
    @JoinColumn(name = "case_id")
    private Cases caseObj;

    @Column(nullable = false)
    private String conversation_text;

    private LocalDateTime timestamp;

    // Getters and Setters
    public Integer getChat_id() { return chat_id; }
    public void setChat_id(Integer chat_id) { this.chat_id = chat_id; }

    public Cases getCaseObj() { return caseObj; }
    public void setCaseObj(Cases caseObj) { this.caseObj = caseObj; }

    public String getConversation_text() { return conversation_text; }
    public void setConversation_text(String conversation_text) { this.conversation_text = conversation_text; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
