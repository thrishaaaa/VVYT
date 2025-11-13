package com.legal_system.mediation.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class ChatbotService {

    // This RestTemplate is used to make HTTP calls to your Python service
    private final RestTemplate restTemplate = new RestTemplate();
    
    // The URL of your running Python app.py
    private final String classifierApiUrl = "http://localhost:5000/analyze";

    public String analyzeCase(String userQuery) {
        try {
            // 1. Create the JSON request body, e.g., {"message": "user query here"}
            String requestBody = "{\"message\": \"" + userQuery.replace("\"", "\\\"") + "\"}";

            // 2. Set headers to specify we are sending JSON
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(requestBody, headers);

            // 3. Call the Python API and get the response as a String
            String jsonResponse = restTemplate.postForObject(classifierApiUrl, entity, String.class);

            // 4. Parse the JSON response from Python and format it as HTML
            return formatClassifierResponse(jsonResponse);

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: Could not connect to the analysis service. Please try again later.";
        }
    }

    // Inside the formatClassifierResponse method in ChatbotService.java

    private String formatClassifierResponse(String jsonResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(jsonResponse);

            String category = root.path("category").asText("Other");
            
            // **** THIS IS THE LINE TO FIX ****
            // String resolutionPath = root.path("resolution_path").asText("Mediation"); // OLD - WRONG KEY
            String resolutionPath = root.path("suggested_resolution_path").asText("Mediation"); // NEW - CORRECT KEY
            // **** END FIX ****
            
            String guidance = root.path("guidance").asText("See details below."); // Get guidance from JSON

            // Get documents_needed (handle potential null/empty array)
            List<String> documents = new ArrayList<>();
            if (root.hasNonNull("documents_needed") && root.get("documents_needed").isArray()) {
                 documents = StreamSupport.stream(
                    root.path("documents_needed").spliterator(), false)
                    .map(JsonNode::asText)
                    .collect(Collectors.toList());
            }

            // Get next_steps (handle potential null/empty array)
             List<String> nextSteps = new ArrayList<>();
             if (root.hasNonNull("next_steps") && root.get("next_steps").isArray()) {
                nextSteps = StreamSupport.stream(
                    root.path("next_steps").spliterator(), false)
                    .map(JsonNode::asText)
                    .collect(Collectors.toList());
             }

            // Build HTML - Ensure resolutionPath is used correctly
            StringBuilder html = new StringBuilder();
            html.append("<b>Case Analysis Results:</b><br/>");
            html.append("<b>Category:</b> ").append(htmlEscape(category)).append("<br/>");
            html.append("<b>Recommended Path:</b> ").append(htmlEscape(resolutionPath)).append("<br/>"); // Uses the corrected variable
            html.append("<b>Guidance:</b> ").append(htmlEscape(guidance)).append("<br/>");

            html.append("<b>Required Documents:</b><ul>");
            if (!documents.isEmpty()) {
                 for (String doc : documents) {
                     html.append("<li>").append(htmlEscape(doc)).append("</li>");
                 }
            } else {
                 html.append("<li>No specific documents listed.</li>");
            }
            html.append("</ul>");

            html.append("<b>Next Steps:</b><ul>");
            if (!nextSteps.isEmpty()) {
                for (String step : nextSteps) {
                    html.append("<li>").append(htmlEscape(step)).append("</li>");
                }
            } else {
                 html.append("<li>Follow general guidance.</li>");
            }
            html.append("</ul>");

            return html.toString();
        } catch (Exception e) {
            e.printStackTrace(); // Log the actual error
            return "<p>Error parsing analysis response from Python service.</p>";
        }
    }

    // Helper method to escape HTML characters (good practice)
    private String htmlEscape(String input) {
        if (input == null) return "";
        // Basic escaping, consider using a library like Apache Commons Text for more robust escaping if needed
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
}