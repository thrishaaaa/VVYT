package com.legal_system.mediation.repository;

import com.legal_system.mediation.model.CaseParties;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CasePartiesRepository extends JpaRepository<CaseParties, Integer> {
}