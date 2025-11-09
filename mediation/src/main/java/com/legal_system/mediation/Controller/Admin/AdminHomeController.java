package com.legal_system.mediation.Controller.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminHomeController {
    
    @GetMapping("/dashboard")
    public String home(){
        return "Admin/Dashboard/index";
    }
}
