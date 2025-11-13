package com.legal_system.mediation.Controller;

import com.legal_system.mediation.Service.TranslationService; // Import the new service
import org.springframework.beans.factory.annotation.Autowired; // NEW
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
// Note: Removed @Service annotation as this is a @RestController
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class GeneralChatController {

    @Value("${groq.api.key}")
    private String groqApiKey;
    
    @Autowired // NEW: Inject the translation service
    private TranslationService translationService; 
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String MODEL_LANGUAGE = "en"; // Groq model is English-centric

    private final String systemPrompt = """
        You are a helpful and polite assistant for the "Resolve-IT" platform,
        a service for legal dispute mediation in India.
        Answer the user's general questions clearly, concisely, and conversationally.
        Do NOT analyze or classify cases. Your role is only to provide general information
        about mediation, legal terms relevant to mediation, or the Resolve-IT platform.
        Keep your answers helpful and easy to understand. Use standard formatting like numbered lists or bullet points where appropriate for clarity.
        """; // Added a hint for the model to use lists

    private final String groqApiUrl = "https://api.groq.com/openai/v1/chat/completions";

    @PostMapping("/chat/ask")
    // MODIFIED: Added userLanguageCode parameter
    public String askGeneralQuestion(@RequestParam String userQuery, 
                                     @RequestParam(defaultValue = "en") String userLanguageCode) {

        // 1. --- INPUT TRANSLATION: Translate user query to English for Groq ---
        String originalLang = userLanguageCode;
        String processedQuery = userQuery;
        
        if (!MODEL_LANGUAGE.equalsIgnoreCase(originalLang)) {
            // Translate the user's query to English for the Groq API
            processedQuery = translationService.translateText(userQuery, MODEL_LANGUAGE, originalLang);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        // Request Body uses the (potentially) translated query
        String requestBody = """
        {
          "model": "llama-3.1-8b-instant",
          "messages": [
            {
              "role": "system",
              "content": "%s"
            },
            {
              "role": "user",
              "content": "%s"
            }
          ]
        }
        """.formatted(
            systemPrompt.replace("\"", "\\\"").replace("\n", "\\n"),
            processedQuery.replace("\"", "\\\"").replace("\n", "\\n") // Use processedQuery
        );

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            String response = restTemplate.postForObject(groqApiUrl, entity, String.class);
            JsonNode root = objectMapper.readTree(response);
            String plainTextContent = root.path("choices").path(0).path("message").path("content").asText();

            // 2. --- OUTPUT TRANSLATION: Translate Groq response (English) back to user's language ---
            String finalResponse = plainTextContent;
            if (!MODEL_LANGUAGE.equalsIgnoreCase(originalLang)) {
                 finalResponse = translationService.translateText(plainTextContent, originalLang, MODEL_LANGUAGE);
            }

            // Format the (potentially translated) response as HTML
            return formatResponseAsHtml(finalResponse);

        } catch (Exception e) {
            e.printStackTrace();
            // Translate the error message if possible
            String errorMsg = "Error: Could not connect to the Groq API. Please try again.";
            if (!MODEL_LANGUAGE.equalsIgnoreCase(originalLang)) {
                 errorMsg = translationService.translateText(errorMsg, originalLang, MODEL_LANGUAGE);
            }
            return "<p>" + errorMsg + "</p>";
        }
    }

    // *** NEW HELPER METHOD to format plain text to basic HTML ***
    private String formatResponseAsHtml(String plainText) {
        if (plainText == null || plainText.trim().isEmpty()) {
            return "<p>Sorry, I didn't get a response.</p>";
        }

        // 1. Sanitize basic HTML - prevent accidental tag injection from LLM
        String sanitizedText = plainText.replace("<", "&lt;").replace(">", "&gt;");

        // 2. Split into lines for processing
        List<String> lines = Arrays.asList(sanitizedText.split("\\r?\\n")); // Handles Windows/Unix newlines

        StringBuilder html = new StringBuilder();
        boolean inList = false;
        String listType = ""; // "ul" or "ol"

        for (String line : lines) {
            String trimmedLine = line.trim();
            boolean isListItem = trimmedLine.startsWith("* ") || trimmedLine.startsWith("- ");
            boolean isOrderedListItem = trimmedLine.matches("^\\d+\\.\\s+.*"); // Matches "1. Text"

            if (isListItem || isOrderedListItem) {
                String currentListType = isOrderedListItem ? "ol" : "ul";
                if (!inList || !listType.equals(currentListType)) {
                    // Close previous list if type changes or wasn't in a list
                    if (inList) {
                        html.append("</").append(listType).append(">\n");
                    }
                    // Start new list
                    listType = currentListType;
                    html.append("<").append(listType).append(">\n");
                    inList = true;
                }
                // Add list item (remove the bullet/number)
                String listItemContent = isOrderedListItem ?
                    trimmedLine.substring(trimmedLine.indexOf('.') + 1).trim() :
                    trimmedLine.substring(1).trim(); // Remove '*' or '-'
                html.append("  <li>").append(listItemContent).append("</li>\n");
            } else {
                // Not a list item
                if (inList) {
                    // Close the list if we were in one
                    html.append("</").append(listType).append(">\n");
                    inList = false;
                }
                // Treat non-empty lines as paragraphs (or add <br> for single newlines if preferred)
                if (!trimmedLine.isEmpty()) {
                     // Simple approach: wrap each non-list line in <p>
                     html.append("<p>").append(trimmedLine).append("</p>\n");
                } else {
                     // Handle potentially empty lines between paragraphs if needed, e.g., add a <br>
                }
            }
        }

        // Close any open list at the end
        if (inList) {
            html.append("</").append(listType).append(">\n");
        }

        return html.toString();
    }
}