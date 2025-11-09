package com.legal_system.mediation.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/askChatBot")
    public String askChatbotAbtMediation(){
        return "chatbotIndex";
    }

    @GetMapping("/sign-in")
    public String login(){
        return "login";
    }

    @GetMapping("/sign-up")
    public String register(){
        return "redirect:/register";
    }

    @GetMapping("/resolve-it-through-mediation")
    public String index(){
        return "index";
    }

    // Redirect old get-advice link to legal advisors
    @GetMapping("/get-advice")
    public String getAdvice(){
        return "redirect:/legal-advisors";
    }
}