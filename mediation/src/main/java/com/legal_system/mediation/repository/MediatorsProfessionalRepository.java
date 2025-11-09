package com.legal_system.mediation.repository;

import com.legal_system.mediation.model.MediatorProfessionalDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MediatorsProfessionalRepository  extends JpaRepository<MediatorProfessionalDetails,Integer> {
}
