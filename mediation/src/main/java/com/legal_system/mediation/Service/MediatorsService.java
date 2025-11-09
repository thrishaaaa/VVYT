package com.legal_system.mediation.Service;

import com.legal_system.mediation.model.Mediators;
import com.legal_system.mediation.repository.MediatorsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MediatorsService {

    @Autowired
    private MediatorsRepository mediatorsRepository;

    public void addMediators(Mediators mediators){
        mediatorsRepository.save(mediators);
    }

    public List<Mediators> getAllMediators(){
        return mediatorsRepository.findAll();
    }

    public Mediators findMediator(int id){
        Optional<Mediators> theMediator =  mediatorsRepository.findById(id);
        return theMediator.orElse(null);
    }


}
