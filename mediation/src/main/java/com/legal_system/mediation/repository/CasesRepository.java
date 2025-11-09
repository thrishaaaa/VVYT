package com.legal_system.mediation.repository;

import com.legal_system.mediation.model.Cases;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CasesRepository extends JpaRepository<Cases,Integer> {
}
