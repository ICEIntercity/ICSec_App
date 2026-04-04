package com.czintercity.icsec_app.relationships;

import com.czintercity.icsec_app.attack.TechniqueRepository;
import com.czintercity.icsec_app.relationships.techniqueCoverage.DefaultTechniqueCoverage;
import com.czintercity.icsec_app.relationships.techniqueCoverage.TechniqueCoverage;
import jakarta.validation.groups.Default;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class RelationshipController {

    private final TechniqueRepository techniqueRepository;

    public RelationshipController(TechniqueRepository techniqueRepository) {
        this.techniqueRepository = techniqueRepository;
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
}
