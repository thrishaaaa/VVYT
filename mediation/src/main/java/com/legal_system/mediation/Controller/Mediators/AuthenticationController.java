package com.legal_system.mediation.Controller.Mediators;

import com.legal_system.mediation.Service.MediatorsService;
import com.legal_system.mediation.model.Mediators;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/mediators")
public class AuthenticationController {

    @Autowired
    private MediatorsService mediatorsService;
    private Mediators mediators = new Mediators();

    @GetMapping("/sign-in")
    public String signInPage(Model model){
        model.addAttribute("action_path","/mediators/save-mediators");
        model.addAttribute("mediator",this.mediators);
        model.addAttribute("error",false);
        return "Mediators/register";
    }

    @PostMapping("/save-mediators")
    public String saveMediatorsDataIntoDB(
            @ModelAttribute("mediator") Mediators theMediator,
            Model model
    ){
        System.out.println(theMediator.getConfirmPassword()+" "+theMediator.getPassword());
        if(theMediator.getConfirmPassword().equals(theMediator.getPassword())){
            mediatorsService.addMediators(theMediator);
            return "index";
        }
        else{
            model.addAttribute("error",true);
            return "Mediators/register";
        }

    }
}
