package com.legal_system.mediation.Controller.Admin;

import com.legal_system.mediation.model.Cases;
import com.legal_system.mediation.Service.CasesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class CasesController {

    @Autowired
    private CasesService casesService;

    @GetMapping("/track-cases")
    public String trackCases(Model model) {
        int userId = 1; // TODO: Replace with actual logged-in user retrieval
        List<Cases> cases = casesService.getCasesByParty1Id(userId);
        model.addAttribute("cases", cases);
        return "track_cases";
    }
}