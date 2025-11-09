package com.legal_system.mediation.Controller.Admin;

import com.legal_system.mediation.Service.UserService;
import com.legal_system.mediation.model.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/register")  // Add base mapping
public class UserController {

    @Autowired
    private UserService userService;

    // This method ensures userDetails is ALWAYS available
    @ModelAttribute("userDetails")
    public UserDetails getUserDetails() {
        return new UserDetails();
    }

    // Show registration page
    @GetMapping
    public String showRegisterForm() {
        return "register";
    }

    // Handle form submission
    @PostMapping("/save")
    public String registerUser(@ModelAttribute("userDetails") UserDetails userDetails) {
        try {
            userService.saveUser(userDetails);
            return "redirect:/register/success";
        } catch (Exception e) {
            e.printStackTrace();
            return "register"; // Will use the @ModelAttribute method above
        }
    }

    // Success page
    @GetMapping("/success")
    public String registerSuccess() {
        return "register_success";
    }
}