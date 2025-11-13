package com.legal_system.mediation.Controller.Admin;

import com.legal_system.mediation.Service.UserMeetingsService;
import com.legal_system.mediation.model.MeetingSchedule;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/user")
public class UserMeetingsController {

    @Autowired
    private UserMeetingsService userMeetingsService;

    @GetMapping("/meetings")
    public String showUserMeetings(HttpSession session, Model model) {
        // Get logged-in user ID from session
        Integer userId = (Integer) session.getAttribute("loggedInUserId");

        System.out.println("Logged in user ID: " + userId); // Debug line

        if (userId == null) {
            return "redirect:/sign-in";
        }

        // Get all meetings for this user (where they are party1 or party2 in the case)
        List<MeetingSchedule> meetings = userMeetingsService.getUserMeetings(userId);

        // Calculate statistics
        long totalMeetings = meetings.size();
        long upcomingMeetings = meetings.stream()
                .filter(m -> m.getStatus().equals("Scheduled"))
                .count();
        long completedMeetings = meetings.stream()
                .filter(m -> m.getStatus().equals("Completed"))
                .count();

        // Add to model
        model.addAttribute("meetings", meetings);
        model.addAttribute("totalMeetings", totalMeetings);
        model.addAttribute("upcomingMeetings", upcomingMeetings);
        model.addAttribute("completedMeetings", completedMeetings);

        return "user-meetings";
    }
}