package com.legal_system.mediation.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.legal_system.mediation.Service.TranslationService;

import org.springframework.beans.factory.annotation.Autowired; // NEW

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class ChatbotService {

    @Autowired // NEW: Inject the translation service
    private TranslationService translationService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String classifierApiUrl = "http://localhost:5000/analyze";
    private final String MODEL_LANGUAGE = "en"; // Python model always outputs English

    // MODIFIED: Added targetLanguageCode parameter
    public String analyzeCase(String userQuery, String targetLanguageCode) {
        if (targetLanguageCode == null || targetLanguageCode.trim().isEmpty()) {
            targetLanguageCode = MODEL_LANGUAGE;
        }

        try {
            // 1. Create the JSON request body (userQuery is assumed to be English here from the Controller)
            String requestBody = "{\"message\": \"" + userQuery.replace("\"", "\\\"") + "\"}";

            // 2. Set headers
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(requestBody, headers);

            // 3. Call the Python API (response is in English)
            String jsonResponse = restTemplate.postForObject(classifierApiUrl, entity, String.class);

            // 4. Parse, format, and translate the response
            return formatAndTranslateClassifierResponse(jsonResponse, targetLanguageCode);

        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = "Error: Could not connect to the analysis service. Please try again later.";
            if (!MODEL_LANGUAGE.equalsIgnoreCase(targetLanguageCode)) {
                errorMessage = translationService.translateText(errorMessage, targetLanguageCode, MODEL_LANGUAGE);
            }
            return errorMessage;
        }
    }

    // MODIFIED: Renamed and added targetLanguageCode parameter
    private String formatAndTranslateClassifierResponse(String jsonResponse, String targetLanguageCode) {
        System.out.println("DEBUG: Target Language Code received: " + targetLanguageCode);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(jsonResponse);

            // All fields are in English (MODEL_LANGUAGE)
            String category = root.path("category").asText("Other");
            // FIXED: Using correct key from app.py
            String resolutionPath = root.path("suggested_resolution_path").asText("Mediation"); 
            String guidance = root.path("guidance").asText("See details below.");

            // Get documents_needed (list of English strings)
            List<String> documents = new ArrayList<>();
            if (root.hasNonNull("documents_needed") && root.get("documents_needed").isArray()) {
                 documents = StreamSupport.stream(
                    root.path("documents_needed").spliterator(), false)
                    .map(JsonNode::asText)
                    .collect(Collectors.toList());
            }

            // Get next_steps (list of English strings)
             List<String> nextSteps = new ArrayList<>();
             if (root.hasNonNull("next_steps") && root.get("next_steps").isArray()) {
                nextSteps = StreamSupport.stream(
                    root.path("next_steps").spliterator(), false)
                    .map(JsonNode::asText)
                    .collect(Collectors.toList());
             }

            // --- TRANSLATION STEP: Translate English fields to the target language ---
            if (!MODEL_LANGUAGE.equalsIgnoreCase(targetLanguageCode)) {
                category = translationService.translateText(category, targetLanguageCode, MODEL_LANGUAGE);
                resolutionPath = translationService.translateText(resolutionPath, targetLanguageCode, MODEL_LANGUAGE);
                guidance = translationService.translateText(guidance, targetLanguageCode, MODEL_LANGUAGE);
                
                // Translate list items
                documents = documents.stream()
                    .map(doc -> translationService.translateText(doc, targetLanguageCode, MODEL_LANGUAGE))
                    .collect(Collectors.toList());
                    
                nextSteps = nextSteps.stream()
                    .map(step -> translationService.translateText(step, targetLanguageCode, MODEL_LANGUAGE))
                    .collect(Collectors.toList());
            }
            // --- END TRANSLATION STEP ---
            
            // Translate static labels for a truly multilingual response
            String label_results = (!MODEL_LANGUAGE.equalsIgnoreCase(targetLanguageCode)) ? 
                                   translationService.translateText("Case Analysis Results:", targetLanguageCode, MODEL_LANGUAGE) : "Case Analysis Results:";
            String label_category = (!MODEL_LANGUAGE.equalsIgnoreCase(targetLanguageCode)) ? 
                                    translationService.translateText("Category:", targetLanguageCode, MODEL_LANGUAGE) : "Category:";
            String label_path = (!MODEL_LANGUAGE.equalsIgnoreCase(targetLanguageCode)) ? 
                                translationService.translateText("Recommended Path:", targetLanguageCode, MODEL_LANGUAGE) : "Recommended Path:";
            String label_guidance = (!MODEL_LANGUAGE.equalsIgnoreCase(targetLanguageCode)) ? 
                                    translationService.translateText("Guidance:", targetLanguageCode, MODEL_LANGUAGE) : "Guidance:";
            String label_docs = (!MODEL_LANGUAGE.equalsIgnoreCase(targetLanguageCode)) ? 
                                translationService.translateText("Required Documents:", targetLanguageCode, MODEL_LANGUAGE) : "Required Documents:";
            String label_next = (!MODEL_LANGUAGE.equalsIgnoreCase(targetLanguageCode)) ? 
                                translationService.translateText("Next Steps:", targetLanguageCode, MODEL_LANGUAGE) : "Next Steps:";
            String label_no_docs = (!MODEL_LANGUAGE.equalsIgnoreCase(targetLanguageCode)) ? 
                                   translationService.translateText("No specific documents listed.", targetLanguageCode, MODEL_LANGUAGE) : "No specific documents listed.";
            String label_general_guidance = (!MODEL_LANGUAGE.equalsIgnoreCase(targetLanguageCode)) ? 
                                            translationService.translateText("Follow general guidance.", targetLanguageCode, MODEL_LANGUAGE) : "Follow general guidance.";


            // Build HTML
            StringBuilder html = new StringBuilder();
            html.append("<b>").append(htmlEscape(label_results)).append("</b><br/>");
            html.append("<b>").append(htmlEscape(label_category)).append("</b> ").append(htmlEscape(category)).append("<br/>");
            html.append("<b>").append(htmlEscape(label_path)).append("</b> ").append(htmlEscape(resolutionPath)).append("<br/>");
            html.append("<b>").append(htmlEscape(label_guidance)).append("</b> ").append(htmlEscape(guidance)).append("<br/>");

            html.append("<b>").append(htmlEscape(label_docs)).append("</b><ul>");
            if (!documents.isEmpty()) {
                 for (String doc : documents) {
                     html.append("<li>").append(htmlEscape(doc)).append("</li>");
                 }
            } else {
                 html.append("<li>").append(htmlEscape(label_no_docs)).append("</li>");
            }
            html.append("</ul>");

            html.append("<b>").append(htmlEscape(label_next)).append("</b><ul>");
            if (!nextSteps.isEmpty()) {
                for (String step : nextSteps) {
                    html.append("<li>").append(htmlEscape(step)).append("</li>");
                }
            } else {
                 html.append("<li>").append(htmlEscape(label_general_guidance)).append("</li>");
            }
            html.append("</ul>");

            return html.toString();
        } catch (Exception e) {
            e.printStackTrace(); 
            return "<p>Error parsing analysis response from Python service.</p>";
        }
    }

    // Helper method to escape HTML characters (good practice)
    private String htmlEscape(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
}