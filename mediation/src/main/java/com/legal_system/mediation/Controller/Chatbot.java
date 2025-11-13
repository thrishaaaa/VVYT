package com.legal_system.mediation.Controller;

import com.legal_system.mediation.Service.ChatbotService; // Import the new service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

@RestController
public class Chatbot {
    
    // Inject your new ChatbotService
    @Autowired
    private ChatbotService chatbotService;

    // This endpoint is used by chatbotIndex.html
    @PostMapping("/processUserRequest")
    public String processUserRequest(HttpServletRequest request){
        String userQuery = request.getParameter("userQuery");
        
        // Call the service to get the formatted HTML response
        return chatbotService.analyzeCase(userQuery); 
    }
}