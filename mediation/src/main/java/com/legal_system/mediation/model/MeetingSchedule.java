package com.legal_system.mediation.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "meeting_schedule")
public class MeetingSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDate meetingDate;
    private LocalTime meetingTime;
    private String description;
    private String status = "Scheduled";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "mediator_id")
    private Mediators mediator;

    // NEW: Add the case relationship
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "case_id")
    private Cases caseEntity;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public LocalDate getMeetingDate() { return meetingDate; }
    public void setMeetingDate(LocalDate meetingDate) { this.meetingDate = meetingDate; }

    public LocalTime getMeetingTime() { return meetingTime; }
    public void setMeetingTime(LocalTime meetingTime) { this.meetingTime = meetingTime; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Mediators getMediator() { return mediator; }
    public void setMediator(Mediators mediator) { this.mediator = mediator; }

    // NEW getter and setter
    public Cases getCaseEntity() { return caseEntity; }
    public void setCaseEntity(Cases caseEntity) { this.caseEntity = caseEntity; }
}