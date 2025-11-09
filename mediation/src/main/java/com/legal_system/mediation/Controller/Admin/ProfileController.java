package com.legal_system.mediation.Controller.Admin;
import com.legal_system.mediation.Service.ProfileService;
import com.legal_system.mediation.model.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @GetMapping
    public String viewProfile(Model model) {
        int userId = 1; // Replace with actual user session logic
        UserDetails user = profileService.getUserById(userId);
        model.addAttribute("user", user);
        return "profile";
    }

    @GetMapping("/edit")
    public String editProfilePage(Model model) {
        int userId = 1;
        UserDetails user = profileService.getUserById(userId);
        model.addAttribute("user", user);
        return "profile_edit";
    }

    @PostMapping("/edit")
    public String updateProfile(@ModelAttribute UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        try {
            int userId = 1;
            userDetails.setId(userId);
            profileService.updateProfile(userDetails);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update profile: " + e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/delete")
    public String deleteAccount(@RequestParam String confirmation,
                                RedirectAttributes redirectAttributes) {
        try {
            if (!"DELETE".equals(confirmation)) {
                redirectAttributes.addFlashAttribute("error", "Please type DELETE to confirm");
                return "redirect:/profile";
            }
            int userId = 1;
            profileService.deleteAccount(userId);
            return "redirect:/sign-up?deleted=true";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete account: " + e.getMessage());
        }
        return "redirect:/profile";
    }
}
