package com.legal_system.mediation.repository;

import com.legal_system.mediation.model.CaseDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaseDocumentRepository extends JpaRepository<CaseDocument, Integer> {
    // Custom query to find all documents for a specific case
    // List<CaseDocument> findByCaseObj_CaseId(Integer caseId); 
}