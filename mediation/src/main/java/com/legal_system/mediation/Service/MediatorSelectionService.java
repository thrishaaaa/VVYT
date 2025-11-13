package com.legal_system.mediation.Service;

import com.legal_system.mediation.model.MediatorProfessionalDetails;
import com.legal_system.mediation.model.Mediators;
import com.legal_system.mediation.repository.MediatorsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MediatorSelectionService {

    @Autowired
    private MediatorsRepository mediatorsRepository;

    /**
     * Selects the best available mediator based on a priority-based ranking system.
     * Priority: is_verified (Proxy for Availability) > Success Rate (Rating Proxy) > Successful Cases > Years of Experience (all Descending)
     *
     * @return The best Mediators object, or Optional.empty() if no available mediators are found.
     */
    public Optional<Mediators> selectBestMediator() {
        // 1. Fetch all mediators to perform in-memory sorting
        List<Mediators> allMediators = mediatorsRepository.findAll();

        // 2. Filter for Eligibility: 
        // A) Must have professional details for ranking (prevents NullPointerException)
        // B) Must be explicitly verified (using the actual boolean field, proxy for true availability)
        List<Mediators> availableMediators = allMediators.stream()
                // Filter A: Must have professional details attached for ranking
                .filter(m -> m.getProfessionalDetails() != null)
                // Filter B: Must be explicitly verified (is_verified = TRUE)
                .filter(m -> m.getIs_verified() != null && m.getIs_verified())
                .collect(Collectors.toList());

        if (availableMediators.isEmpty()) {
            return Optional.empty();
        }

        // 3. Apply the Multi-criteria Ranking (Comparator Chaining)
        Comparator<Mediators> rankingComparator = Comparator
                // Comparator 1 (Highest Priority): Rating (using successRate as proxy) - Highest first
                .comparing((Mediators m) -> {
                    MediatorProfessionalDetails details = m.getProfessionalDetails();
                    return details.getSuccessRate();
                }, Comparator.reverseOrder())

                // Comparator 2: Successful Cases (casesWon) - Highest first
                .thenComparing((Mediators m) -> {
                    MediatorProfessionalDetails details = m.getProfessionalDetails();
                    return details.getCasesWon();
                }, Comparator.reverseOrder())

                // Comparator 3: Years of Experience - Highest first
                .thenComparing((Mediators m) -> {
                    MediatorProfessionalDetails details = m.getProfessionalDetails();
                    return details.getYearsExperience();
                }, Comparator.reverseOrder());

        // 4. Find the single best mediator
        return availableMediators.stream()
                .max(rankingComparator);
    }
}