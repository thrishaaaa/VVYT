package com.legal_system.mediation.Service;


import com.legal_system.mediation.model.CaseParties;
import com.legal_system.mediation.repository.CasePartiesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class CasePartiesService {
    @Autowired
    private CasePartiesRepository CasePartiesRepository;

    public void addCaseParties(CaseParties CaseParties){
        CasePartiesRepository.save(CaseParties);
    }

    public List<CaseParties> getAllCaseParties(){
        return CasePartiesRepository.findAll();
    }

    public CaseParties findMediator(int id){
        Optional<CaseParties> theCaseParties =  CasePartiesRepository.findById(id);
        return theCaseParties.orElse(null);
    } 
}
