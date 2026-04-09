package com.czintercity.icsec_app.controls;

import com.czintercity.icsec_app.attack.TechniqueRepository;
import com.czintercity.icsec_app.form.EditControlForm;
import com.czintercity.icsec_app.relationships.techniqueCoverage.DefaultTechniqueCoverage;
import com.czintercity.icsec_app.relationships.techniqueCoverage.DefaultTechniqueCoverageRepository;
import com.czintercity.icsec_app.topics.Topic;
import com.czintercity.icsec_app.topics.TopicRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
public class ControlController {
    private static final Logger log = LoggerFactory.getLogger(ControlController.class);

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
     * @return control/allControls template
     */
    @GetMapping("/control/all")
    public String listControls(Model model) {
        log.trace("listControls called.");
        Iterable<Control> controls = controlRepository.findAll();
        model.addAttribute("controls", controls);
        return "control/allControls";
    }

    /**
     * Initiates the creation of a new control.
     *
     * @param model Spring Boot Model attribute
     * @return Rendering of controlForm.html
     */
    @GetMapping("/control/new")
    public String newControl(Model model) {
        log.trace("newControl called.");
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
    public String showControl(@PathVariable UUID id, Model model) {
        log.trace("ShowControl(id={}) called.", id);
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
    public String editControl(@PathVariable UUID id, Model model) {
        log.trace("EditControl called for id: {}", id);
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

        // Get control id (will be zero if id is not set)
        UUID controlId = controlForm.getControlId() == null ? null : controlForm.getControlId();
        log.trace("SaveControl called for id: {}.", controlId);

        if(!result.hasErrors()) {
            Control toSave;
            log.info("Saving control with id: {}", controlId);
            // Check if we are saving existing or creating new
            if(controlForm.getControlId() != null) {
                Optional<Control> foundControl = controlRepository.findById(controlForm.getControlId());
                if(foundControl.isPresent()) {
                    log.debug("Found control with id: {}, populating", foundControl.get().getId());
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
            toSave.setCostIndex(controlForm.getControlCostIndex());
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

            // set id and success message
            controlId = toSave.getId();
            redirectAttributes.addFlashAttribute("formSuccess", "Control updated successfully");
            log.info("Control id {} updated successfully.", controlId);
        }
        else{
            // set error message
            redirectAttributes.addFlashAttribute("formError", result.getAllErrors());
            log.warn("Control with id {} has not been updated due to form validation error.", controlId);
        }

        if(controlId != null) {
            redirectAttributes.addAttribute("id", controlId);
            return "redirect:/control/edit/{id}";
        }
        else {
            return "redirect:/control/new";
        }
    }

    @DeleteMapping("control/delete/{id}")
    public ResponseEntity<Void> deleteControl(@PathVariable UUID id) {
        Optional<Control> target = controlRepository.findById(id);

        if (target.isPresent()) {
            Control toDelete = target.get();

            controlRepository.delete(toDelete);
            log.info("Deleted control ID: {}", toDelete.getId());

            HttpHeaders headers = new HttpHeaders();
            // Pass a 'status' parameter in the URL
            headers.add("HX-Redirect", "/control/all?deleted=true");
            return new ResponseEntity<>(headers, HttpStatus.OK);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Control with id " + id + " not found");
        }
    }
}
