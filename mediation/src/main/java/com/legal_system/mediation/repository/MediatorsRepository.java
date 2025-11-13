package com.legal_system.mediation.repository;

import com.legal_system.mediation.model.Mediators;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MediatorsRepository  extends JpaRepository<Mediators,Integer> {
    Optional<Mediators> findByEmail(String email);
}

