package com.legal_system.mediation.model;

import jakarta.persistence.*;

@Entity
@Table(name = "caseparties")
public class CaseParties {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer casepart_id;

    @ManyToOne
    @JoinColumn(name = "case_id")
    private Cases caseObj;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserDetails user;

    @Column(nullable = false)
    private String role;

    // Getters and Setters
    public Integer getCasepart_id() { return casepart_id; }
    public void setCasepart_id(Integer casepart_id) { this.casepart_id = casepart_id; }

    public Cases getCaseObj() { return caseObj; }
    public void setCaseObj(Cases caseObj) { this.caseObj = caseObj; }

    public UserDetails getUser() { return user; }
    public void setUser(UserDetails user) { this.user = user; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
