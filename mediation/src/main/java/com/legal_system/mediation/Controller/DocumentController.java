package com.legal_system.mediation.Controller;

import com.legal_system.mediation.Service.CasesService;
import com.legal_system.mediation.Service.DocumentService;
import com.legal_system.mediation.model.Cases;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller
public class DocumentController {

    @Autowired
    private CasesService casesService; // To fetch case details
    
    @Autowired
    private DocumentService documentService; // To handle file storage and metadata

    // This is a simplified list of required documents derived from the Modelfile. 
    // In a real application, this should be stored in the DB and linked by category.
    private static final List<String> MATRIMONIAL_DOCS = Arrays.asList(
        "Marriage certificate", 
        "Address proof of both spouses", 
        "ID proof of both spouses", 
        "Marriage photographs", 
        "Grounds evidence (medical, financial, messages)", 
        "Child's birth certificate", 
        "Income proof"
    );

    @GetMapping("/upload-documents/{caseId}")
    public String showUploadPage(@PathVariable Integer caseId, Model model) {
        Cases caseObj = casesService.findCase(caseId);
        if (caseObj == null) {
            return "redirect:/resolve-it-through-mediation"; // Case not found
        }
        
        // Determine required documents based on case type
        List<String> requiredDocs = getRequiredDocumentsForCaseType(caseObj.getCase_type());
        
        model.addAttribute("case", caseObj);
        model.addAttribute("requiredDocs", requiredDocs);
        
        // Add existing documents here if they were already uploaded
        // model.addAttribute("uploadedDocuments", documentService.getDocumentsForCase(caseId));

        return "document_upload"; // Need to create this Thymeleaf template
    }

    @PostMapping("/upload-document")
    public String handleDocumentUpload(@RequestParam("caseId") Integer caseId,
                                       @RequestParam("documentType") String documentType,
                                       @RequestParam("file") MultipartFile file,
                                       RedirectAttributes redirectAttributes) {
        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please select a file to upload.");
                return "redirect:/upload-documents/" + caseId;
            }

            documentService.saveDocument(caseId, documentType, file);
            
            redirectAttributes.addFlashAttribute("success", 
                file.getOriginalFilename() + " uploaded successfully for " + documentType + "!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Upload failed: " + e.getMessage());
        }

        return "redirect:/upload-documents/" + caseId;
    }
    
    // Simple helper method to determine the required document list
    private List<String> getRequiredDocumentsForCaseType(String caseType) {
        if ("Matrimonial disputes".equalsIgnoreCase(caseType)) {
            return MATRIMONIAL_DOCS;
        }
        // Add logic for other case types here...
        return List.of("General proof of dispute", "ID proof of parties");
    }
}