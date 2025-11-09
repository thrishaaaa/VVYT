package com.legal_system.mediation.Controller.Admin;

import com.legal_system.mediation.Service.LegalAdvisorService;
import com.legal_system.mediation.model.LegalAdvisor;
import com.legal_system.mediation.model.AdvisorMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/legal-advisors")
public class LegalAdvisorController {

    @Autowired
    private LegalAdvisorService advisorService;

    /**
     * Show list of all legal advisors
     */
    @GetMapping
    public String listAdvisors(@RequestParam(required = false) String search,
                               @RequestParam(required = false) String university,
                               @RequestParam(required = false) String year,
                               Model model) {

        List<LegalAdvisor> advisors;

        // Apply filters
        if (search != null && !search.isEmpty()) {
            advisors = advisorService.searchAdvisorsByName(search);
        } else if (university != null && !university.isEmpty()) {
            advisors = advisorService.getAdvisorsByUniversity(university);
        } else if (year != null && !year.isEmpty()) {
            advisors = advisorService.getAdvisorsByYear(year);
        } else {
            advisors = advisorService.getAvailableAdvisors();
        }

        model.addAttribute("advisors", advisors);
        model.addAttribute("search", search);
        model.addAttribute("university", university);
        model.addAttribute("year", year);

        return "legal_advisors_list";
    }

    /**
     * Show advisor profile details
     */
    @GetMapping("/{advisorId}")
    public String viewAdvisorProfile(@PathVariable Integer advisorId, Model model) {
        LegalAdvisor advisor = advisorService.getAdvisorById(advisorId);
        model.addAttribute("advisor", advisor);
        return "legal_advisor_profile";
    }

    /**
     * Show messaging interface
     */
    @GetMapping("/{advisorId}/chat")
    public String chatWithAdvisor(@PathVariable Integer advisorId, Model model) {
        // TODO: Get actual logged-in user ID
        Integer userId = 1;

        LegalAdvisor advisor = advisorService.getAdvisorById(advisorId);
        List<AdvisorMessage> conversation = advisorService.getConversation(userId, advisorId);

        model.addAttribute("advisor", advisor);
        model.addAttribute("messages", conversation);
        model.addAttribute("userId", userId);

        return "advisor_chat";
    }

    /**
     * Send message to advisor
     */
    @PostMapping("/{advisorId}/send-message")
    public String sendMessage(@PathVariable Integer advisorId,
                              @RequestParam String message,
                              RedirectAttributes redirectAttributes) {
        try {
            // TODO: Get actual logged-in user ID
            Integer userId = 1;

            advisorService.sendMessage(userId, advisorId, message, "USER");
            redirectAttributes.addFlashAttribute("success", "Message sent successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to send message: " + e.getMessage());
        }

        return "redirect:/legal-advisors/" + advisorId + "/chat";
    }
}