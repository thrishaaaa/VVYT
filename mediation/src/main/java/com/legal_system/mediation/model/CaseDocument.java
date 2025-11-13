package com.legal_system.mediation.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "case_documents")
public class CaseDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer document_id;

    // Link to the Cases table
    @ManyToOne
    @JoinColumn(name = "case_id", nullable = false)
    private Cases caseObj;

    @Column(nullable = false)
    private String document_type; // e.g., "Marriage certificate"

    @Column(nullable = false)
    private String file_name; // Original filename

    // Path where the file is stored on the server/local disk
    @Column(nullable = false)
    private String stored_path; 

    private String status = "Pending"; // Uploaded, Verified, Rejected

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    // --- Getters and Setters ---
    public Integer getDocument_id() { return document_id; }
    public void setDocument_id(Integer document_id) { this.document_id = document_id; }

    public Cases getCaseObj() { return caseObj; }
    public void setCaseObj(Cases caseObj) { this.caseObj = caseObj; }

    public String getDocument_type() { return document_type; }
    public void setDocument_type(String document_type) { this.document_type = document_type; }

    public String getFile_name() { return file_name; }
    public void setFile_name(String file_name) { this.file_name = file_name; }

    public String getStored_path() { return stored_path; }
    public void setStored_path(String stored_path) { this.stored_path = stored_path; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}