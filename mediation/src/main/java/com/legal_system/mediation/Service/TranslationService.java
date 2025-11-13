package com.legal_system.mediation.Service;

import com.google.cloud.translate.v3.LocationName;
import com.google.cloud.translate.v3.TranslateTextRequest;
import com.google.cloud.translate.v3.TranslateTextResponse;
import com.google.cloud.translate.v3.TranslationServiceClient;
import com.google.cloud.translate.v3.Translation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service
public class TranslationService {

    // --- Configuration ---
    // Note: The project ID is usually auto-detected via credentials.file, but we include it here.
    @Value("${google.cloud.project-id}")
    private String projectId;
    
    // Choose the location for the translation service
    private final String location = "global"; 
    private final String MODEL_LANGUAGE = "en"; 
    
    // Google Client instance
    private TranslationServiceClient client;

    /**
     * Initializes the TranslationServiceClient after bean creation.
     * This is the correct way to initialize resources in a Spring Service.
     */
    @PostConstruct
public void initializeClient() {
    try {
        this.client = TranslationServiceClient.create();
        System.out.println("Google Translation Client initialized successfully.");
    } catch (Exception e) {
        // --- ADD THIS LINE ---
        e.printStackTrace(); 
        // ---------------------
        System.err.println("FATAL: Failed to initialize Google Translation Client: " + e.getMessage());
        // Do NOT set this.client = null; it is null by default if the try block fails.
    }
}

    /**
     * Closes the client before the bean is destroyed.
     */
    @PreDestroy
    public void closeClient() {
        if (this.client != null) {
            this.client.close();
            System.out.println("Google Translation Client closed.");
        }
    }

    /**
     * Translates text from source language to a target language using the V3 API.
     * @param text The text to translate.
     * @param targetLanguageCode The target language (e.g., "en", "hi").
     * @param sourceLanguageCode The source language (optional hint, API can auto-detect).
     * @return The translated text.
     */
    public String translateText(String text, String targetLanguageCode, String sourceLanguageCode) {
        if (this.client == null) {
            System.err.println("Error: Translation Client is not initialized.");
            return text; // Fallback
        }
        if (text == null || text.trim().isEmpty() || targetLanguageCode.equalsIgnoreCase(MODEL_LANGUAGE)) {
            return text; 
        }

        try {
            LocationName parent = LocationName.of(projectId, location);

            TranslateTextRequest request = TranslateTextRequest.newBuilder()
                    .setParent(parent.toString())
                    .setTargetLanguageCode(targetLanguageCode)
                    .addContents(text)
                    // Optional: Provide source language code if available (API auto-detects if missing)
                    .setSourceLanguageCode(sourceLanguageCode != null ? sourceLanguageCode : "")
                    .build();

            TranslateTextResponse response = client.translateText(request);
            
            // Assuming we only send one text chunk:
            if (response.getTranslationsCount() > 0) {
                 Translation translation = response.getTranslations(0);
                 System.out.println("LOG: Translated from " + translation.getDetectedLanguageCode() + " to " + targetLanguageCode);
                 return translation.getTranslatedText();
            }
            return text; // Return original on empty response

        } catch (Exception e) {
            System.err.println("Translation API Error: " + e.getMessage());
            // Fallback: return the original text on API error
            return text;
        }
    }

    /**
     * Conceptual method to detect language (for input validation/hints).
     * The actual translation API call handles auto-detection best.
     * This remains as a basic fallback if necessary outside the core translateText loop.
     */
    public String detectLanguage(String text) {
        // You would typically use a separate API call for detection if needed outside of the translateText logic, 
        // but since the translate API auto-detects, we keep the original simplified logic here:
        if (text != null && (text.toLowerCase().matches(".*(\\bmujhe\\b|\\bkya\\b|\\bnamaste\\b).*") || text.toLowerCase().contains("hindi"))) {
             return "hi"; 
        }
        return MODEL_LANGUAGE; // Default to English
    }
}