package com.legal_system.mediation.Service;

import com.legal_system.mediation.model.ChatbotLogs;
import com.legal_system.mediation.model.ChatbotLogs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChatbotLogsService {
    @Autowired
    private com.legal_system.mediation.repository.ChatbotLogsRepository ChatbotLogsRepository;

    public void addChatbotLogs(ChatbotLogs ChatbotLogs){
        ChatbotLogsRepository.save(ChatbotLogs);
    }

    public List<ChatbotLogs> getAllChatbotLogs(){
        return ChatbotLogsRepository.findAll();
    }

    public ChatbotLogs findMediator(int id){
        Optional<ChatbotLogs> theChatbotLogs =  ChatbotLogsRepository.findById(id);
        return theChatbotLogs.orElse(null);
    }
}
