package com.legal_system.mediation.repository;

import org.springframework.stereotype.Repository;
import com.legal_system.mediation.model.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

@Repository
public interface UserDetailsRepository extends JpaRepository<UserDetails, Integer>{

    // Find user by email (useful for login and profile lookup)
    Optional<UserDetails> findByEmail(String email);

    // Check if email already exists (useful for registration validation)
    boolean existsByEmail(String email);
}
