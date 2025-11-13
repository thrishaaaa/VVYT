package com.legal_system.mediation.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cases")
public class Cases {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "case_id")
    private Integer case_id;

    @Column(name = "case_type", nullable = false)
    private String case_type;

    @Column(name = "description")
    private String description;

    @Column(name = "status")
    private String status = "Open";

    @Column(name = "created_at")
    private LocalDateTime created_at;

    @Column(name = "resolved_at")
    private LocalDateTime resolved_at;

    @ManyToOne
    @JoinColumn(name = "mediator_id")
    private Mediators mediator;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "party1_id")
    private UserDetails party1;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "party2_id")
    private UserDetails party2;

    // Constructors
    public Cases() {
    }

    public Cases(String case_type, String description, String status) {
        this.case_type = case_type;
        this.description = description;
        this.status = status;
        this.created_at = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getCase_id() {
        return case_id;
    }

    public void setCase_id(Integer case_id) {
        this.case_id = case_id;
    }

    public String getCase_type() {
        return case_type;
    }

    public void setCase_type(String case_type) {
        this.case_type = case_type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    public LocalDateTime getResolved_at() {
        return resolved_at;
    }

    public void setResolved_at(LocalDateTime resolved_at) {
        this.resolved_at = resolved_at;
    }

    public Mediators getMediator() {
        return mediator;
    }

    public void setMediator(Mediators mediator) {
        this.mediator = mediator;
    }

    public UserDetails getParty1() {
        return party1;
    }

    public void setParty1(UserDetails party1) {
        this.party1 = party1;
    }

    public UserDetails getParty2() {
        return party2;
    }

    public void setParty2(UserDetails party2) {
        this.party2 = party2;
    }

    // Helper method to check if case is resolved
    public boolean isResolved() {
        return "Resolved".equalsIgnoreCase(this.status);
    }

    // Helper method to mark case as resolved
    public void markAsResolved() {
        this.status = "Resolved";
        this.resolved_at = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Cases{" +
                "case_id=" + case_id +
                ", case_type='" + case_type + '\'' +
                ", status='" + status + '\'' +
                ", mediator=" + (mediator != null ? mediator.getId() : null) +
                '}';
    }
}