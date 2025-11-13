package com.legal_system.mediation.Service;

import com.legal_system.mediation.model.CaseDocument;
import com.legal_system.mediation.model.Cases;
import com.legal_system.mediation.repository.CaseDocumentRepository;
import com.legal_system.mediation.repository.CasesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DocumentService {

    @Autowired
    private CaseDocumentRepository documentRepository;

    @Autowired
    private CasesRepository casesRepository;

    // Define the base directory for storing files (relative to the application root)
    private final String uploadDir = "uploads/";

    public void saveDocument(Integer caseId, String documentType, MultipartFile file) throws Exception {
        Cases caseObj = casesRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        // 1. Handle File Storage
        // Create a unique directory for the case
        Path caseUploadPath = Paths.get(uploadDir + "case_" + caseId);
        Files.createDirectories(caseUploadPath); // Creates directory if it doesn't exist

        // Create a unique filename to prevent overwriting
        String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = caseUploadPath.resolve(uniqueFileName);
        
        // Copy the file content to the target location
        Files.copy(file.getInputStream(), filePath);

        // 2. Save Metadata to DB (PostgreSQL)
        CaseDocument doc = new CaseDocument();
        doc.setCaseObj(caseObj);
        doc.setDocument_type(documentType);
        doc.setFile_name(file.getOriginalFilename());
        doc.setStored_path(filePath.toString());
        doc.setUploadedAt(LocalDateTime.now());
        doc.setStatus("Uploaded");
        
        documentRepository.save(doc);
    }
}