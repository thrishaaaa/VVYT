package com.legal_system.mediation.Service;

import com.legal_system.mediation.model.Cases;
import com.legal_system.mediation.repository.CasesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CasesService {

    @Autowired
    private CasesRepository casesRepository;

    // Add a new case
    public void addCases(Cases caseObj) {
        casesRepository.save(caseObj);
    }

    // Get all cases
    public List<Cases> getAllCases() {
        return casesRepository.findAll();
    }

    // Find a specific case by ID
    public Cases findCase(int id) {
        Optional<Cases> theCase = casesRepository.findById(id);
        return theCase.orElse(null);
    }

    // Get cases where user is party1
    // Inside your service class
    public List<Cases> getCasesByParty1Id(int userId) {
        return casesRepository.findAll().stream()
                .filter(c -> c.getParty1() != null && c.getParty1().getId() == userId)
                .toList();
    }

}
