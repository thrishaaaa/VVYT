package com.legal_system.mediation.Service;

import com.legal_system.mediation.model.MediatorProfessionalDetails;
import com.legal_system.mediation.model.Mediators;
import com.legal_system.mediation.repository.MediatorsProfessionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MediatorsProfessionalService {

    @Autowired
    private MediatorsProfessionalRepository mediatorsProfessionalRepository;

    public MediatorProfessionalDetails findMediator(int id){
        Optional<MediatorProfessionalDetails> theMediator =  mediatorsProfessionalRepository.findById(id);
        return theMediator.orElse(null);
    }
}
