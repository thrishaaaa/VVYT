package com.legal_system.mediation.repository;

import com.legal_system.mediation.model.Cases;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CasesRepository extends JpaRepository<Cases, Integer> {

    // Find all cases for a specific mediator
    List<Cases> findByMediatorId(Integer mediatorId);

    // Find cases by mediator and status
    List<Cases> findByMediatorIdAndStatus(Integer mediatorId, String status);

    // Count total cases for a mediator
    Long countByMediatorId(Integer mediatorId);

    // Count cases by status for a mediator
    Long countByMediatorIdAndStatus(Integer mediatorId, String status);

    // NEW: Count cases where status is NOT the given value (for active cases count)
    Long countByMediatorIdAndStatusNot(Integer mediatorId, String status);
}