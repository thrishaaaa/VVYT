package com.legal_system.mediation.Controller;

import com.legal_system.mediation.Service.ChatbotService; 
import com.legal_system.mediation.Service.TranslationService; // Import the new service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

@RestController
public class Chatbot {
    
    // Inject your services
    @Autowired
    private ChatbotService chatbotService;

    @Autowired
    private TranslationService translationService; // Inject the Translation Service

    private final String MODEL_LANGUAGE = "en"; 

    // This endpoint must now also accept the language code (e.g., from a hidden field in the form)
    @PostMapping("/processUserRequest")
    public String processUserRequest(HttpServletRequest request){
        String userQuery = request.getParameter("userQuery");
        // NEW: Get the desired output language code. Assume 'en' if not provided by the UI.
        String targetLanguageCode = request.getParameter("targetLanguageCode");
        
        if (targetLanguageCode == null || targetLanguageCode.trim().isEmpty()) {
            // If language is not provided by UI, try to detect it for the output
            targetLanguageCode = translationService.detectLanguage(userQuery); 
        }
        
        String originalLanguageCode = targetLanguageCode; // Use this as the source language hint for input translation

        // 1. Translate user input to English for the classifier model (if necessary)
        String queryForClassifier = userQuery;
        if (!MODEL_LANGUAGE.equalsIgnoreCase(originalLanguageCode)) {
             // Translate the user's query to English for the Python classifier
             queryForClassifier = translationService.translateText(userQuery, MODEL_LANGUAGE, originalLanguageCode);
        }
        
        // 2. Call the service with the English query and the desired output language
        // The service will now handle translating the output back to targetLanguageCode.
        return chatbotService.analyzeCase(queryForClassifier, targetLanguageCode); 
    }
}