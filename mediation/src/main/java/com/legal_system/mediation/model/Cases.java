package com.legal_system.mediation.model;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cases")
public class Cases {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer case_id;

    @Column(nullable = false)
    private String case_type;

    private String description;

    private String status = "Open";

    private LocalDateTime created_at;

    private LocalDateTime resolved_at;

    @ManyToOne
    @JoinColumn(name = "mediator_id")
    private Mediators mediator;

    @ManyToOne
    @JoinColumn(name = "party1_id")
    private UserDetails party1;

    @ManyToOne
    @JoinColumn(name = "party2_id")
    private UserDetails party2;

    // Getters and Setters
    public Integer getCase_id() { return case_id; }
    public void setCase_id(Integer case_id) { this.case_id = case_id; }

    public String getCase_type() { return case_type; }
    public void setCase_type(String case_type) { this.case_type = case_type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreated_at() { return created_at; }
    public void setCreated_at(LocalDateTime created_at) { this.created_at = created_at; }

    public LocalDateTime getResolved_at() { return resolved_at; }
    public void setResolved_at(LocalDateTime resolved_at) { this.resolved_at = resolved_at; }

    public Mediators getMediator() { return mediator; }
    public void setMediator(Mediators mediator) { this.mediator = mediator; }

    public UserDetails getParty1() { return party1; }
    public void setParty1(UserDetails party1) { this.party1 = party1; }

    public UserDetails getParty2() { return party2; }
    public void setParty2(UserDetails party2) { this.party2 = party2; }
}