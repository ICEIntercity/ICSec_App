package com.czintercity.icsec_app.controls;

import com.czintercity.icsec_app.attack.TechniqueRepository;
import com.czintercity.icsec_app.form.EditControlForm;
import com.czintercity.icsec_app.relationships.techniqueCoverage.DefaultTechniqueCoverage;
import com.czintercity.icsec_app.relationships.techniqueCoverage.DefaultTechniqueCoverageRepository;
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

import java.util.*;

@Controller
public class ControlController {
    @Autowired
    private ControlRepository controlRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private DefaultTechniqueCoverageRepository defaultTechniqueCoverageRepository;

    @Autowired
    private TechniqueRepository techniqueRepository;

    /**
     * Shows an overview of all controls to the user - including MITRE coverage.
     *
     * @param model Spring boot model
     * @return control/controlList template
     */
    @GetMapping("/control/all")
    public String listControls(Model model) {
        Iterable<Control> controls = controlRepository.findAll();
        model.addAttribute("controls", controls);
        return "control/controlList";
    }


    @GetMapping("/control/new")
    public String newControl(Model model) {
        model.addAttribute("controlForm", new EditControlForm());
        model.addAttribute("topics", topicRepository.findAll());
        model.addAttribute("techniques", techniqueRepository.findAll());
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
            EditControlForm editControlForm = new EditControlForm(control.get());

            model.addAttribute("controlForm", editControlForm);
            model.addAttribute("topics", topicRepository.findAll());
            return "control/controlForm";
        }
        else {
            model.addAttribute("error", "http.cat/404");
            return "error";
        }
    }

    @PostMapping("/control/edit")
    public String saveControl(@Valid @ModelAttribute("controlForm") EditControlForm controlForm, BindingResult result, Model model) {
        if(!result.hasErrors()) {
            Control toSave;

            // Check if we are saving existing or creating new
            if(controlForm.getControlId() != null) {
                Optional<Control> foundControl = controlRepository.findById(controlForm.getControlId());
                if(foundControl.isPresent()) {
                    // Give the existing set
                    toSave = foundControl.get();

                    // Clear existing coverage (preparing to overwrite)
                    defaultTechniqueCoverageRepository.deleteAll(defaultTechniqueCoverageRepository.findByControl(toSave));
                }
                else {
                    model.addAttribute("error", "http.cat/404");
                    return "error";
                }
            }
            else {
                toSave = new Control();
            }

            // Handle simple params
            toSave.setName(controlForm.getControlName());
            toSave.setDescription(controlForm.getControlDescription());
            toSave.setTopic(controlForm.getTopic());
            toSave.setReferences(controlForm.getReferences());

            List<DefaultTechniqueCoverage> techniqueCoverage = new ArrayList<>();

            // Handle controls (We've previously deleted all for this technique)
            for(DefaultTechniqueCoverage coverage : controlForm.getDefaultTechniqueCoverage()) {
                coverage = defaultTechniqueCoverageRepository.save(coverage);
                techniqueCoverage.add(coverage);
            }
            toSave.setDefaultTechniqueCoverage(techniqueCoverage);
            controlRepository.save(toSave);

            // Display success
            model.addAttribute("formSuccess", "Control updated successfully.");
        }
        else{
            model.addAttribute("formError", result.getAllErrors());
        }

        model.addAttribute("controlForm", controlForm);
        model.addAttribute("topics", topicRepository.findAll());
        return "control/controlForm";
    }
}
