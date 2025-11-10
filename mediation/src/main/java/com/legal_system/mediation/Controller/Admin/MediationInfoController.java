package com.legal_system.mediation.Controller.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MediationInfoController {

    @GetMapping("/mediation-info")
    public String showMediationInfo(Model model) {
        // You can add any dynamic data here if needed in the future
        return "mediation_info";
    }
}
