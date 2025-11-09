package com.legal_system.mediation.model;

import com.legal_system.mediation.model.Cases;
import com.legal_system.mediation.model.Mediators;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "caseallotment")
public class CaseAllotment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer allotment_id;

    @ManyToOne
    @JoinColumn(name = "case_id")
    private Cases caseObj;

    @ManyToOne
    @JoinColumn(name = "mediator_id")
    private Mediators mediator;

    private LocalDateTime allotment_date;

    private String status = "Assigned";

    // Getters and Setters
    public Integer getAllotment_id() { return allotment_id; }
    public void setAllotment_id(Integer allotment_id) { this.allotment_id = allotment_id; }

    public Cases getCaseObj() { return caseObj; }
    public void setCaseObj(Cases caseObj) { this.caseObj = caseObj; }

    public Mediators getMediator() { return mediator; }
    public void setMediator(Mediators mediator) { this.mediator = mediator; }

    public LocalDateTime getAllotment_date() { return allotment_date; }
    public void setAllotment_date(LocalDateTime allotment_date) { this.allotment_date = allotment_date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}