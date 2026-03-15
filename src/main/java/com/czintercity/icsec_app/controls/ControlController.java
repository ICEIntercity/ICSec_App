package com.czintercity.icsec_app.controls;

import com.czintercity.icsec_app.topics.TopicRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;

@Controller
public class ControlController {
    @Autowired
    private ControlRepository controlRepository;

    @Autowired
    private TopicRepository topicRepository;

    @GetMapping("/control/all")
    public String listControls(Model model) {
        Iterable<Control> controls = controlRepository.findAll();
        model.addAttribute("controls", controls);
        return "control/controlList";
    }


    @GetMapping("/control/new")
    public String newControl(Model model) {
        model.addAttribute("control", new Control());
        model.addAttribute("topics", topicRepository.findAll());
        return "control/controlForm";
    }

    @GetMapping("/control/{id}")
    public String showControl(@PathVariable Long id, Model model) {
        Optional<Control> control = controlRepository.findById(id);
        if(control.isPresent()){
            model.addAttribute("control", control.get());
            return "control/controlView";
        }
        else {
            model.addAttribute("error", "http.cat/404");
            return "error";
        }
    }

    @GetMapping("/control/edit/{id}")
    public String editControl(@PathVariable Long id, Model model) {
        Optional<Control> control = controlRepository.findById(id);
        if(control.isPresent()){
            model.addAttribute("control", control.get());
            model.addAttribute("topics", topicRepository.findAll());
            return "control/controlForm";
        }
        else {
            model.addAttribute("error", "http.cat/404");
            return "error";
        }
    }

    @PostMapping("/control/edit")
    public String saveControl(@Valid @ModelAttribute("Control") Control control, BindingResult result, Model model) {
        if(!result.hasErrors()) {
            controlRepository.save(control);
            model.addAttribute("formSuccess", "Control updated successfully.");
        }
        else{
            model.addAttribute("formError", result.getAllErrors());
        }

        model.addAttribute("control", control);
        model.addAttribute("topics", topicRepository.findAll());
        return "control/controlForm";
    }
}
