package com.czintercity.icsec_app.controls;

import com.czintercity.icsec_app.attack.TechniqueRepository;
import com.czintercity.icsec_app.form.EditControlForm;
import com.czintercity.icsec_app.relationships.techniqueCoverage.DefaultTechniqueCoverage;
import com.czintercity.icsec_app.relationships.techniqueCoverage.DefaultTechniqueCoverageRepository;
import com.czintercity.icsec_app.topics.TopicRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    /**
     * Initiates the creation of a new cotnrol.
     *
     * @param model Spring Boot Model attribute
     * @return Rendering of controlForm.html
     */
    @GetMapping("/control/new")
    public String newControl(Model model) {
        model.addAttribute("controlForm", new EditControlForm());
        model.addAttribute("topics", topicRepository.findAll());
        model.addAttribute("techniques", techniqueRepository.findAll());
        return "control/controlForm";
    }

    /**
     * Displays the details of control in read-only mode.
     *
     * @param id ID of control to display
     * @param model Spring Boot model attribute
     * @return Rendering of controlView.html (or error 404)
     */
    @GetMapping("/control/{id}")
    public String showControl(@PathVariable Long id, Model model) {
        Optional<Control> control = controlRepository.findById(id);
        if(control.isPresent()){
            model.addAttribute("control", control.get());
            return "control/controlView";
        }
        else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Control with id " + id + " not found");
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
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Control with id " + id + " not found");
    }

    @PostMapping("/control/edit")
    public String saveControl(@Valid @ModelAttribute("controlForm") EditControlForm controlForm, RedirectAttributes redirectAttributes, BindingResult result) {
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
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Control with id " + controlForm.getControlId() + " does not exist.");
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

            // Generate an ID (inefficient, but whatever)
            toSave = controlRepository.save(toSave);

            // Handle controls (We've previously deleted all for this technique)
            for(DefaultTechniqueCoverage coverage : controlForm.getDefaultTechniqueCoverage()) {

                // Skip empty/deleted rows (we don't need them)
                if(coverage.isBlank()){
                    continue;
                }

                coverage.setControl(toSave);
                coverage = defaultTechniqueCoverageRepository.save(coverage);
                techniqueCoverage.add(coverage);
            }
            toSave.setDefaultTechniqueCoverage(techniqueCoverage);
            toSave = controlRepository.save(toSave);

            // Display success
            redirectAttributes.addFlashAttribute("formSuccess", "Control updated successfully");
        }
        else{
            redirectAttributes.addFlashAttribute("formError", result.getAllErrors());
        }

        redirectAttributes.addAttribute("id", controlForm.getControlId());
        return "redirect:/control/edit/{id}";
    }
}
