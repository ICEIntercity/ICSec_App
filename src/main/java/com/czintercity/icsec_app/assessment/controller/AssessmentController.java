package com.czintercity.icsec_app.assessment.controller;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import com.czintercity.icsec_app.assessment.dto.AssessmentDTO;
import com.czintercity.icsec_app.assessment.dto.TechniquePrioritiesFormDTO;
import com.czintercity.icsec_app.assessment.entity.Assessment;
import com.czintercity.icsec_app.assessment.repository.AssessmentRepository;
import com.czintercity.icsec_app.assessment.service.AssessmentService;
import com.czintercity.icsec_app.attack.service.AttackService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

/**
 * Handles HTTP requests for creating, editing, and saving {@link Assessment} records,
 * as well as managing MITRE ATT&amp;CK technique prioritisation within an assessment.
 */
@Controller
public class AssessmentController {
    private final AssessmentRepository assessmentRepository;
    private final AssessmentService assessmentService;
    private final AttackService attackService;

    public AssessmentController(AssessmentRepository assessmentRepository, AssessmentService assessmentService,
                                AttackService attackService) {
        this.assessmentRepository = assessmentRepository;
        this.assessmentService = assessmentService;
        this.attackService = attackService;
    }

    /**
     * Renders the assessment edit form.
     * If {@code assessmentId} is provided and found, the existing assessment is loaded;
     * otherwise a new {@link AssessmentDTO} with a generated name is prepared.
     *
     * @param assessmentId optional ID of an existing assessment; {@code null} for a new one
     */
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

    /**
     * Persists the assessment form submission.
     * When {@code redirectTo} is {@code "prioritize"}, redirects to the MITRE prioritisation view;
     * otherwise redirects back to the assessment edit view.
     *
     * @param redirectTo optional hint controlling the post-save redirect destination
     */
    @PostMapping("/assessment/save")
    public String saveAssessment(@ModelAttribute AssessmentDTO dto,
                                 @org.springframework.web.bind.annotation.RequestParam(required = false) String redirectTo) {
        Assessment assessment = assessmentService.saveAssessment(dto);
        if ("prioritize".equals(redirectTo)) {
            return "redirect:/assessment/" + assessment.getId() + "/prioritize";
        }
        return "redirect:/assessment/" + assessment.getId();
    }

    /**
     * Renders the MITRE ATT&amp;CK technique coverage assessment view for the given assessment,
     * pre-populating each technique card with any previously saved priority value.
     */
    @GetMapping("/assessment/{assessmentId}/prioritize")
    public String prioritizeMitre(Model model, @PathVariable UUID assessmentId) {
        Optional<Assessment> existing = assessmentRepository.findById(assessmentId);
        if (existing.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assessment not found");
        }
        model.addAttribute("assessment", new AssessmentDTO(existing.get()));
        model.addAttribute("tacticsMap", attackService.getTacticsWithTechniques());
        model.addAttribute("existingPriorities", assessmentService.getTechniquePriorities(assessmentId));
        return "assessment/mitreAssessment";
    }

    /**
     * Persists technique priority scores for the given assessment.
     * When {@code redirectTo} is {@code "result"}, redirects to the coverage heatmap;
     * otherwise redirects back to the prioritisation view.
     *
     * @param redirectTo optional hint controlling the post-save redirect destination
     */
    @PostMapping("/assessment/{assessmentId}/prioritize")
    public String saveTechniquePriorities(@PathVariable UUID assessmentId,
                                          @ModelAttribute TechniquePrioritiesFormDTO formDTO,
                                          @org.springframework.web.bind.annotation.RequestParam(required = false) String redirectTo) {
        assessmentService.saveTechniquePriorities(assessmentId, formDTO.getPriorities());
        if ("result".equals(redirectTo)) {
            return "redirect:/assessment/" + assessmentId + "/result";
        }
        return "redirect:/assessment/" + assessmentId + "/prioritize";
    }
}
