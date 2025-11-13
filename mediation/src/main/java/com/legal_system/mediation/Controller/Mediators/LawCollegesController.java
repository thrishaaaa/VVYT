package com.legal_system.mediation.Controller.Mediators;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LawCollegesController {

    @GetMapping("/lawcolleges")
    public String lawColleges() {
        return "Mediators/lawcolleges";
    }
}