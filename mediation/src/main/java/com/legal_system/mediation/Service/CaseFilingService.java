package com.legal_system.mediation.Service;

import com.legal_system.mediation.model.*;
import com.legal_system.mediation.repository.CasesRepository;
import com.legal_system.mediation.repository.ChatbotLogsRepository;
import com.legal_system.mediation.repository.PartyRepository;
import com.legal_system.mediation.repository.UserDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CaseFilingService {

    @Autowired
    private UserDetailsRepository userRepository;

    @Autowired
    private PartyRepository partyRepository;

    @Autowired
    private CasesRepository casesRepository;

    @Autowired
    private ChatbotLogsRepository chatbotLogsRepository;

    // --- NEW INJECTION ---
    @Autowired
    private MediatorSelectionService mediatorSelectionService; 
    // --- END NEW INJECTION ---


    /**
     * Finds the Party entity for a given email. ... (logic remains the same)
     */
    private Party findOrCreateParty(String email) {
        Optional<UserDetails> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            // User found, create a linked Party entry
            UserDetails user = userOptional.get();
            
            Party newParty = new Party();
            newParty.setUser(user);
            newParty.setName(user.getName());
            newParty.setEmail(user.getEmail());
            newParty.setPhone(user.getPhone_no());
            newParty.setIs_registered(true);
            return partyRepository.save(newParty);
        } else {
            // User not found, create an external Party entry
            Party externalParty = new Party();
            externalParty.setName("External Party"); // Placeholder name
            externalParty.setEmail(email);
            externalParty.setIs_registered(false);
            return partyRepository.save(externalParty);
        }
    }

    /**
     * Handles the complete case filing process.
     * ...
     */
    @Transactional
    public Cases fileNewCase(Integer party1Id, String party2Email, String caseDescription, String caseCategory, String conversationLog) {
        // 1. Get Party 1 (logged-in user)
        UserDetails party1User = userRepository.findById(party1Id)
                .orElseThrow(() -> new RuntimeException("Logged-in user not found."));

        // 2. Find or Create Party 2
        Party party2 = findOrCreateParty(party2Email);
        UserDetails party2User = party2.getUser(); 
        
        // --- NEW STEP: 3. Select Best Mediator ---
        Optional<Mediators> selectedMediator = mediatorSelectionService.selectBestMediator();
        // Assign the selected mediator or null if none are available
        Mediators assignedMediator = selectedMediator.orElse(null); 
        // --- END NEW STEP ---


        // 4. Create new Case
        Cases newCase = new Cases();
        newCase.setCase_type(caseCategory);
        newCase.setDescription(caseDescription);
        newCase.setParty1(party1User);
        newCase.setParty2(party2User);
        newCase.setMediator(assignedMediator); // <-- ASSIGNED HERE
        newCase.setCreated_at(LocalDateTime.now());
        newCase.setStatus("Open");

        Cases savedCase = casesRepository.save(newCase);

        // 5. Log the conversation with the new case_id
        ChatbotLogs log = new ChatbotLogs();
        log.setCaseObj(savedCase);
        log.setConversation_text(conversationLog);
        log.setTimestamp(LocalDateTime.now());
        chatbotLogsRepository.save(log);

        return savedCase;
    }
}