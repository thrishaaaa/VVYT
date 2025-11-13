package com.legal_system.mediation.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // ✅ Landing Page (first page)
    @GetMapping("/")
    public String homePage() {
        return "home"; // home.html will be your new first page
    }

    @GetMapping("/askChatBot")
    public String askChatbotAbtMediation() {
        return "chatbotIndex";
    }

    @GetMapping("/general-chat")
    public String generalChat() {
        return "GeneralChat"; 
    }


    /*@GetMapping("/sign-up")
    public String register() {
        return "redirect:/register";
    }*/

    @GetMapping("/user/sign-up")
    public String userSignUp() {
        return "redirect:/register"; // user registration page
    }



    @GetMapping("/resolve-it-through-mediation")
    public String index() {
        return "index"; // your dashboard page (index.html)
    }

    // Redirect old get-advice link to legal advisors
    @GetMapping("/get-advice")
    public String getAdvice() {
        return "redirect:/legal-advisors";
    }
}
