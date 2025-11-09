package com.legal_system.mediation.model;

import jakarta.persistence.*;

@Entity
@Table(name = "party")
public class Party {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer party_id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserDetails user;

    @Column(nullable = false)
    private String name;

    private String email;

    private String phone;

    private Boolean is_registered = false;

    // Getters and Setters
    public Integer getParty_id() { return party_id; }
    public void setParty_id(Integer party_id) { this.party_id = party_id; }

    public UserDetails getUser() { return user; }
    public void setUser(UserDetails user) { this.user = user; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Boolean getIs_registered() { return is_registered; }
    public void setIs_registered(Boolean is_registered) { this.is_registered = is_registered; }
}
