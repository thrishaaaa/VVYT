package com.legal_system.mediation.model;

import jakarta.persistence.*;

@Entity
@Table(name = "mediator_professional_details")
public class MediatorProfessionalDetails {

    @Id
    @Column(name = "mediator_id")
    private int mediatorId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "mediator_id")
    private Mediators mediator;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "cases_handled")
    private Integer casesHandled = 0;

    @Column(name = "cases_won")
    private Integer casesWon = 0;

    @Column(name = "success_rate")
    private Double successRate = 0.0;

    @Column(name = "license_number", unique = true)
    private String licenseNumber;

    @Column(name = "years_experience")
    private Integer yearsExperience;

    @Column(columnDefinition = "TEXT")
    private String education;

    @Column(columnDefinition = "TEXT")
    private String bio;
    // Store JSON as String
    @Column(name = "certifications", columnDefinition = "jsonb")
    private String certifications;

    @Column(name = "expertise_areas", columnDefinition = "jsonb")
    private String expertiseAreas;

    @Column(name = "specializations", columnDefinition = "jsonb")
    private String specializations;

    // --- Getters & Setters ---
    public int getMediatorId() { return mediatorId; }
    public void setMediatorId(int mediatorId) { this.mediatorId = mediatorId; }

    public Mediators getMediator() { return mediator; }
    public void setMediator(Mediators mediator) { this.mediator = mediator; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Integer getCasesHandled() { return casesHandled; }
    public void setCasesHandled(Integer casesHandled) { this.casesHandled = casesHandled; }

    public Integer getCasesWon() { return casesWon; }
    public void setCasesWon(Integer casesWon) { this.casesWon = casesWon; }

    public Double getSuccessRate() { return successRate; }
    public void setSuccessRate(Double successRate) { this.successRate = successRate; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public Integer getYearsExperience() { return yearsExperience; }
    public void setYearsExperience(Integer yearsExperience) { this.yearsExperience = yearsExperience; }

    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getCertifications() { return certifications; }
    public void setCertifications(String certifications) { this.certifications = certifications; }

    public String getExpertiseAreas() { return expertiseAreas; }
    public void setExpertiseAreas(String expertiseAreas) { this.expertiseAreas = expertiseAreas; }

    public String getSpecializations() { return specializations; }
    public void setSpecializations(String specializations) { this.specializations = specializations; }
}
