package com.czintercity.icsec_app.controls.controller;

import com.czintercity.icsec_app.attack.repository.TechniqueRepository;
import com.czintercity.icsec_app.controls.entity.Control;
import com.czintercity.icsec_app.controls.repository.ControlRepository;
import com.czintercity.icsec_app.relationships.controlRelationship.*;
import com.czintercity.icsec_app.relationships.controlRelationship.dto.ControlRelationshipDTO;
import com.czintercity.icsec_app.relationships.techniqueCoverage.entity.TechniqueCoverage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

/**
 * Controller providing HTMX fragment endpoints used by the control edit form.
 * Handles dynamic addition and editing of technique coverage rows and control relationship rows
 * without full page reloads.
 */
@Controller
public class EditControlFormController {

    private final TechniqueRepository techniqueRepository;
    private final ControlRepository controlRepository;

    public EditControlFormController(TechniqueRepository techniqueRepository, ControlRepository controlRepository) {
        this.techniqueRepository = techniqueRepository;
        this.controlRepository = controlRepository;
    }

    /** Returns the modal fragment for adding a new control relationship to the edit form. */
    @GetMapping("/controlRelationship/add")
    public String newControlRelationshipModal(@RequestParam Integer arrayLength, Model model){
        model.addAttribute("controlRelationship", new ControlRelationshipDTO(ControlRelationshipType.DEPENDENCY));
        model.addAttribute("controls", controlRepository.findAll());
        model.addAttribute("relationshipTypes", ControlRelationshipType.values());
        model.addAttribute("index", null);
        model.addAttribute("arrayLength", arrayLength);
        return "fragments/controlRelationship :: controlRelationshipModal";
    }

    /** Returns the modal fragment pre-filled for editing an existing control relationship at the given list index. */
    @GetMapping("/controlRelationship/edit")
    public String updateControlDependencyModal(@RequestParam Integer index, ControlRelationshipDTO dependency, Model model){
        model.addAttribute("controlRelationship", dependency);
        model.addAttribute("controls", controlRepository.findAll());
        model.addAttribute("index", index);
        return "fragments/controlRelationship :: controlRelationshipModal";
    }

    /** Returns a rendered relationship row fragment after the user confirms a relationship in the modal. */
    @PostMapping("/controlRelationship/row")
    public String updateControlRelationshipRow(@RequestParam Integer index,
                                               @RequestParam ControlRelationshipType type,
                                               @RequestParam UUID targetId,
                                               Model model) {

        Optional<Control> targetControl = controlRepository.findById(targetId);
        if(targetControl.isEmpty()) {
            throw new IllegalArgumentException("Target control doesn't exist.");
        }

        Control targetControlObj = targetControl.get();

        ControlRelationshipDTO dto = new ControlRelationshipDTO();
        dto.setTargetId(targetId);
        dto.setType(type);
        dto.setTargetName(targetControlObj.getName());
        dto.setTargetCode(targetControlObj.getTopic().getCode() + "-" + targetControlObj.getDisplayId());

        model.addAttribute("controlRelationship", dto);
        model.addAttribute("index", index);
        model.addAttribute("listName", "outgoingRelationships");

        return "fragments/controlRelationship :: controlRelationshipRow";
    }

    /** Returns the modal fragment for adding a new technique coverage entry to the edit form. */
    @GetMapping("/techniqueCoverage/add")
    public String newTechniqueCoverageModal(@RequestParam Integer arrayLength, Model model){
        model.addAttribute("techniqueCoverage", new TechniqueCoverage());
        model.addAttribute("techniques", techniqueRepository.findAll());
        model.addAttribute("index", null);
        model.addAttribute("arrayLength", arrayLength);
        return "fragments/techniqueCoverage :: techniqueCoverageModal";
    }

    /** Returns the modal fragment pre-filled for editing an existing technique coverage entry at the given list index. */
    @GetMapping("/techniqueCoverage/edit")
    public String updateTechniqueCoverageModal(@RequestParam Integer index, TechniqueCoverage coverage, Model model){
        model.addAttribute("index", index);
        model.addAttribute("techniqueCoverage", coverage);
        model.addAttribute("techniques", techniqueRepository.findAll());
        return "fragments/techniqueCoverage :: techniqueCoverageModal";
    }

    /** Returns a rendered technique coverage row fragment after the user confirms an entry in the modal. */
    @PostMapping("/techniqueCoverage/row")
    public String updateTechniqueCoverageRow(@RequestParam(required = false) Integer index, TechniqueCoverage coverage, Model model){
        model.addAttribute("index", index);
        model.addAttribute("coverage", coverage);
        return "fragments/techniqueCoverage :: techniqueCoverageRow";
    }

    /** Returns an empty reference row fragment for appending a new URL/reference entry to the edit form. */
    @GetMapping("/control/fragments/reference-row")
    public String getReferenceRow() {
        return "fragments/reference :: referenceRow";
    }
}
