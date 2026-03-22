package com.czintercity.icsec_app.relationships;

import com.czintercity.icsec_app.attack.TechniqueRepository;
import com.czintercity.icsec_app.relationships.techniqueCoverage.DefaultTechniqueCoverage;
import jakarta.validation.groups.Default;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RelationshipController {

    private final TechniqueRepository techniqueRepository;

    public RelationshipController(TechniqueRepository techniqueRepository) {
        this.techniqueRepository = techniqueRepository;
    }

    @GetMapping("/techniqueCoverage/add")
    public String newTechniqueCoverageModal(Model model){
        model.addAttribute("techniqueCoverage", new DefaultTechniqueCoverage());
        model.addAttribute("techniques", techniqueRepository.findAll());
        model.addAttribute("index", null);
        return "fragments/techniqueCoverage :: techniqueCoverageModal";
    }

    @GetMapping("/techniqueCoverage/edit")
    public String updateTechniqueCoverageModal(@RequestParam Integer index, @ModelAttribute DefaultTechniqueCoverage coverage, Model model){
        model.addAttribute("index", index);
        model.addAttribute("techniqueCoverage", coverage);
        return "fragments/techniqueCoverage :: techniqueCoverageModal";
    }

    @PostMapping("/techniqueCoverage/row")
    public String updateTechniqueCoverageRow(@RequestParam(required = false) Integer index, DefaultTechniqueCoverage coverage, Model model){
        model.addAttribute("index", index);
        model.addAttribute("coverage", coverage);
        return "fragments/techniqueCoverage :: techniqueCoverageRow";
    }
}
