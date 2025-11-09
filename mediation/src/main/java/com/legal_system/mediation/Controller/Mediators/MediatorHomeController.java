package com.legal_system.mediation.Controller.Mediators;

import com.legal_system.mediation.Service.MediatorsService;
import com.legal_system.mediation.model.Mediators;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/mediators")
public class MediatorHomeController {

    @Autowired
    private MediatorsService mediatorService;

    //  List all mediators
    @GetMapping("/mediators-profiles")
    public String showAllMediators(Model model) {
        model.addAttribute("mediators", mediatorService.getAllMediators());
        return "Mediators/index";
    }

    //  View Mediator profile
    @GetMapping("/{id}")
    public String viewMediatorProfile(@PathVariable int id, Model model) {
        Mediators mediator = mediatorService.findMediator(id);
        model.addAttribute("mediator", mediator);
        return "Mediators/mediatorsProfile";
    }

    //  Schedule page (no id here — static mapping)
    @GetMapping("/schedule")
    public String showSchedulePage(Model model) {
        model.addAttribute("mediators", mediatorService.getAllMediators());
        model.addAttribute("meetings", null); // Replace with meetingService later
        return "Mediators/schedule";
    }

    //  Handle scheduling (POST)
    @PostMapping("/schedule")
    public String scheduleMeeting(@RequestParam String title,
                                  @RequestParam int mediatorId,
                                  @RequestParam String date,
                                  @RequestParam String time,
                                  @RequestParam(required = false) String description) {
        System.out.println("Meeting scheduled with Mediator ID: " + mediatorId);
        // Save to DB when MeetingService is ready
        return "redirect:/mediators/schedule";
    }
}
