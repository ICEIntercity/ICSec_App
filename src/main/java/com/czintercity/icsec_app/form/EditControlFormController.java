package com.czintercity.icsec_app.form;

import com.czintercity.icsec_app.attack.repository.TechniqueRepository;
import com.czintercity.icsec_app.controls.entity.Control;
import com.czintercity.icsec_app.controls.repository.ControlRepository;
import com.czintercity.icsec_app.relationships.controlRelationship.*;
import com.czintercity.icsec_app.relationships.controlRelationship.dto.ControlRelationshipDTO;
import com.czintercity.icsec_app.relationships.techniqueCoverage.entity.DefaultTechniqueCoverage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@Controller
public class EditControlFormController {

    private final TechniqueRepository techniqueRepository;
    private final ControlRepository controlRepository;

    public EditControlFormController(TechniqueRepository techniqueRepository, ControlRepository controlRepository, ControlRelationshipService controlRelationshipService) {
        this.techniqueRepository = techniqueRepository;
        this.controlRepository = controlRepository;
    }

    // Add this to EditControlFormController.java
    @GetMapping("/controlRelationship/add")
    public String newControlRelationshipModal(@RequestParam Integer arrayLength, Model model){
        model.addAttribute("controlRelationship", new ControlRelationshipDTO(ControlRelationshipType.DEPENDENCY));
        model.addAttribute("controls", controlRepository.findAll());
        model.addAttribute("relationshipTypes", ControlRelationshipType.values());
        model.addAttribute("index", null);
        model.addAttribute("arrayLength", arrayLength);
        return "fragments/controlRelationship :: controlRelationshipModal";
    }

    @GetMapping("/controlRelationship/edit")
    public String updateControlDependencyModal(@RequestParam Integer index, ControlRelationshipDTO dependency, Model model){
        model.addAttribute("controlRelationship", dependency);
        model.addAttribute("controls", controlRepository.findAll());
        model.addAttribute("index", index);
        return "fragments/controlRelationship :: controlRelationshipModal";
    }

    // Snippet from EditControlFormController.java

    @PostMapping("/controlRelationship/row")
    public String updateControlRelationshipRow(@RequestParam Integer index,
                                               @RequestParam ControlRelationshipType type,
                                               @RequestParam UUID targetId,
                                               Model model) {

        Optional<Control> targetControl = controlRepository.findById(targetId);
        if(!targetControl.isPresent()) {
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

    @GetMapping("/techniqueCoverage/add")
    public String newTechniqueCoverageModal(@RequestParam Integer arrayLength, Model model){
        model.addAttribute("techniqueCoverage", new DefaultTechniqueCoverage());
        model.addAttribute("techniques", techniqueRepository.findAll());
        model.addAttribute("index", null);
        model.addAttribute("arrayLength", arrayLength);
        return "fragments/techniqueCoverage :: techniqueCoverageModal";
    }

    @GetMapping("/techniqueCoverage/edit")
    public String updateTechniqueCoverageModal(@RequestParam Integer index, DefaultTechniqueCoverage coverage, Model model){
        model.addAttribute("index", index);
        model.addAttribute("techniqueCoverage", coverage);
        model.addAttribute("techniques", techniqueRepository.findAll());
        return "fragments/techniqueCoverage :: techniqueCoverageModal";
    }

    @PostMapping("/techniqueCoverage/row")
    public String updateTechniqueCoverageRow(@RequestParam(required = false) Integer index, DefaultTechniqueCoverage coverage, Model model){
        model.addAttribute("index", index);
        model.addAttribute("coverage", coverage);
        return "fragments/techniqueCoverage :: techniqueCoverageRow";
    }

    // New Delete Endpoint
    @DeleteMapping("/techniqueCoverage/row")
    @ResponseBody
    public String deleteTechniqueCoverageRow() {
        return "";
    }

    @DeleteMapping("/controlRelationship/row")
    @ResponseBody
    public String deleteControlRelationshipRow() {
        return "";
    }

    @GetMapping("/control/fragments/reference-row")
    public String getReferenceRow() {
        return "fragments/reference :: referenceRow";
    }
}
