package com.legal_system.mediation.Service;

import com.legal_system.mediation.model.Cases;
import com.legal_system.mediation.model.Feedback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class FeedbackService {
    @Autowired
    private com.legal_system.mediation.repository.FeedbackRepository FeedbackRepository;

    public void addFeedback(Feedback Feedback){
        FeedbackRepository.save(Feedback);
    }

    public List<Feedback> getAllFeedback(){
        return FeedbackRepository.findAll();
    }

    public Feedback findMediator(int id){
        Optional<Feedback> theFeedback =  FeedbackRepository.findById(id);
        return theFeedback.orElse(null);
    }
}
