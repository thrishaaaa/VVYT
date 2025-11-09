package com.legal_system.mediation.Service;

import com.legal_system.mediation.model.ChatbotLogs;
import com.legal_system.mediation.model.Party;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PartyService {
    @Autowired
    private com.legal_system.mediation.repository.PartyRepository PartyRepository;

    public void addParty(Party Party){
        PartyRepository.save(Party);
    }

    public List<Party> getAllParty(){
        return PartyRepository.findAll();
    }

    public Party findMediator(int id){
        Optional<Party> theParty =  PartyRepository.findById(id);
        return theParty.orElse(null);
    }
}
