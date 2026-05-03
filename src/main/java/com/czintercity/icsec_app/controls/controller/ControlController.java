package com.czintercity.icsec_app.controls.controller;

import com.czintercity.icsec_app.attack.repository.TechniqueRepository;
import com.czintercity.icsec_app.controls.repository.ControlRepository;
import com.czintercity.icsec_app.controls.ControlService;
import com.czintercity.icsec_app.controls.entity.Control;
import com.czintercity.icsec_app.form.EditControlForm;
import com.czintercity.icsec_app.relationships.controlRelationship.repository.ControlRelationshipRepository;
import com.czintercity.icsec_app.relationships.controlRelationship.ControlRelationshipService;
import com.czintercity.icsec_app.relationships.techniqueCoverage.repository.TechniqueCoverageRepository;
import com.czintercity.icsec_app.topics.repository.TopicRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final ControlRepository controlRepository;
    private final TopicRepository topicRepository;
    private final TechniqueRepository techniqueRepository;
    private final ControlService controlService;

    public ControlController(ControlRepository controlRepository, TopicRepository topicRepository, TechniqueCoverageRepository techniqueCoverageRepository, TechniqueRepository techniqueRepository, ControlRelationshipRepository controlRelationshipRepository, ControlRelationshipService controlRelationshipService, ControlService controlService) {
        this.controlRepository = controlRepository;
        this.topicRepository = topicRepository;
        this.techniqueRepository = techniqueRepository;
        this.controlService = controlService;
    }

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
        UUID controlId = controlForm.getControlId();
        log.trace("SaveControl called for id: {}.", controlId);

        if(!result.hasErrors()) {
            try {
                controlId = controlService.createOrUpdateFromForm(controlForm).getId();
            }
            catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Unexpected error while saving control.");
                log.error(e.getMessage(), e);
            }
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
