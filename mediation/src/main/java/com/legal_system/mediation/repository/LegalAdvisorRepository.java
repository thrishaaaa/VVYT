package com.legal_system.mediation.repository;


import com.legal_system.mediation.model.LegalAdvisor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface LegalAdvisorRepository extends JpaRepository<LegalAdvisor, Integer> {

    List<LegalAdvisor> findByAvailabilityStatusTrue();

    List<LegalAdvisor> findByNameContainingIgnoreCase(String name);

    List<LegalAdvisor> findByUniversity(String university);

    List<LegalAdvisor> findByYearOfStudy(String year);

    @Query("SELECT la FROM LegalAdvisor la ORDER BY la.rating DESC")
    List<LegalAdvisor> findTopRatedAdvisors();

}

