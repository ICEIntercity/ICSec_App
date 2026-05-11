package com.czintercity.icsec_app.assessment.controller;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import com.czintercity.icsec_app.assessment.dto.AssessmentDTO;
import com.czintercity.icsec_app.assessment.entity.Assessment;
import com.czintercity.icsec_app.assessment.repository.AssessmentRepository;
import com.czintercity.icsec_app.assessment.service.AssessmentService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class AssessmentController {
    private final AssessmentRepository assessmentRepository;
    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentRepository assessmentRepository, AssessmentService assessmentService) {
        this.assessmentRepository = assessmentRepository;
        this.assessmentService = assessmentService;
    }

    @GetMapping({"/assessment", "/assessment/{assessmentId}"})
    public String editAssessment(Model model, @PathVariable(required = false) UUID assessmentId) {
        AssessmentDTO assessment;

        if(assessmentId != null){
            Optional<Assessment> existingAssessment = assessmentRepository.findById(assessmentId);
            if(existingAssessment.isPresent()){
                assessment = new AssessmentDTO(existingAssessment.get());
            }
            else
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assessment not found.");
        }
        else {
            assessment = new AssessmentDTO();
            assessment.setName("Assessment on " + LocalDateTime.now());
        }


        model.addAttribute("assessment", assessment);
        model.addAttribute("groupedStatusMap", assessmentService.buildDisplayMap(assessment));

        return "assessment/assessmentView";
    }

    @PostMapping("/assessment/save")
    public String saveAssessment(@ModelAttribute AssessmentDTO dto) {
        Assessment assessment = assessmentService.saveAssessment(dto);
        return "redirect:/assessment/" + assessment.getId();
    }

    @GetMapping({"/assessment/{assessmentId}/prioritize", "/assessment/new/prioritize"})
    public String prioritizeMitre(@ModelAttribute AssessmentDTO dto) {
        return "index";
    }
}
