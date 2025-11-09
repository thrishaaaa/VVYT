package com.legal_system.mediation.Service;

import com.legal_system.mediation.model.CaseAllotment;
import com.legal_system.mediation.repository.CaseAllotmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CaseAllotmentService {
    @Autowired
    private CaseAllotmentRepository CaseAllotmentRepository;

    public void addCaseAllotment(CaseAllotment CaseAllotment){
        CaseAllotmentRepository.save(CaseAllotment);
    }

    public List<CaseAllotment> getAllCaseAllotment(){
        return CaseAllotmentRepository.findAll();
    }

    public CaseAllotment findMediator(int id){
        Optional<CaseAllotment> theCaseAllotment =  CaseAllotmentRepository.findById(id);
        return theCaseAllotment.orElse(null);
    }

}
