package com.legal_system.mediation.model;

import jakarta.persistence.*;

import java.sql.Time;
import java.util.Date;

@Table(name="mediators")
@Entity
public class Mediators {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="mediator_id")
    private int mediator_id;

    @OneToOne(mappedBy = "mediator", cascade = CascadeType.ALL)
    private MediatorProfessionalDetails professionalDetails;

    @Column(name="first_name")
    private String firstName;

    @Column(name="last_name")
    private String lastName;

    @Column(name="email")
    private String email;

    @Column(name="password")
    private String password;

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    @Transient
    private String confirmPassword;

    @Column(name="phone")
    private long phone;

    @Column(name="date_of_birth")
    private Date dob;

    @Column(name="gender")
    private String gender;

    @Column(name="time_zone")
    private String timeZone;

    @Column(name="account_status")
    private String status;

    public int getId() {
        return mediator_id;
    }

    public void setId(int id) {
        this.mediator_id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }


    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getPhone() {
        return phone;
    }

    public void setPhone(long phone) {
        this.phone = phone;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getIs_verified() {
        return is_verified;
    }

    public void setIs_verified(Boolean is_verified) {
        this.is_verified = is_verified;
    }

    @Column(name="is_verified")
    private Boolean is_verified;

    public MediatorProfessionalDetails getProfessionalDetails() {
        return professionalDetails;
    }

    public void setProfessionalDetails(MediatorProfessionalDetails professionalDetails) {
        this.professionalDetails = professionalDetails;
    }



}
