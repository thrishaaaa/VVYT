package com.legal_system.mediation.repository;

import com.legal_system.mediation.model.Party;
import com.legal_system.mediation.model.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartyRepository extends JpaRepository<Party, Integer> {
    Party findByUser(UserDetails user); 
}