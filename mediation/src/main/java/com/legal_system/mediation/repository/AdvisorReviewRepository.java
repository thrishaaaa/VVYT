package com.legal_system.mediation.repository;

import com.legal_system.mediation.model.AdvisorReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdvisorReviewRepository extends JpaRepository<AdvisorReview, Integer> {

    List<AdvisorReview> findByLegalAdvisor_AdvisorId(Integer advisorId);

}
