package com.legal_system.mediation.repository;

import com.legal_system.mediation.model.CaseAllotment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaseAllotmentRepository extends JpaRepository<CaseAllotment,Integer> {
}
