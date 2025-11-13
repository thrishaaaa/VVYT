package com.legal_system.mediation.Controller;

import com.legal_system.mediation.Service.CaseFilingService;
import com.legal_system.mediation.model.Cases;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class CaseFilingController {

    @Autowired
    private CaseFilingService caseFilingService;

    @PostMapping("/fileCase")
    public ResponseEntity<Map<String, Object>> fileCase(
            @RequestParam("caseDescription") String caseDescription,
            @RequestParam("caseCategory") String caseCategory,
            @RequestParam("otherPartyEmail") String otherPartyEmail,
            @RequestParam("conversationLog") String conversationLog,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        // 1. Get logged-in user ID (Party 1)
        Integer party1Id = (Integer) session.getAttribute("loggedInUserId");
        if (party1Id == null) {
            response.put("status", "error");
            response.put("message", "User not logged in. Please sign in to file a case.");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // 2. Process case filing
            Cases savedCase = caseFilingService.fileNewCase(
                    party1Id,
                    otherPartyEmail,
                    caseDescription,
                    caseCategory,
                    conversationLog
            );

            response.put("status", "success");
            response.put("message", "Case filed successfully!");
            response.put("caseId", savedCase.getCase_id());
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("status", "error");
            response.put("message", "Failed to file case: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}