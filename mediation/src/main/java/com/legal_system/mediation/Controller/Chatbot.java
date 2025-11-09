package com.legal_system.mediation.Controller;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class Chatbot {
    
    private final ChatModel chatModel;

    Chatbot(ChatModel chatModel){
        this.chatModel= chatModel;
    }

    @PostMapping("/processUserRequest")
    public String processUserRequest(HttpServletRequest request){
        String userQuery = request.getParameter("userQuery");
        // userQuery = "\n "
        String userQueryResponse = chatModel.call(userQuery);
        return userQueryResponse;
    }
}
