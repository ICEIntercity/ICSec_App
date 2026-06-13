package com.czintercity.icsec_app.assessment.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.czintercity.icsec_app.assessment.dto.AssessmentDTO;
import com.czintercity.icsec_app.assessment.dto.TechniquePrioritiesFormDTO;
import com.czintercity.icsec_app.assessment.entity.Assessment;
import com.czintercity.icsec_app.assessment.model.MarginalGain;
import com.czintercity.icsec_app.assessment.repository.AssessmentRepository;
import com.czintercity.icsec_app.assessment.service.AssessmentService;
import com.czintercity.icsec_app.attack.entity.Technique;
import com.czintercity.icsec_app.attack.service.AttackService;
import com.czintercity.icsec_app.controls.entity.Control;
import com.czintercity.icsec_app.topics.entity.Topic;
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
    private final ObjectMapper objectMapper;

    public AssessmentController(AssessmentRepository assessmentRepository, AssessmentService assessmentService,
                                AttackService attackService, ObjectMapper objectMapper) {
        this.assessmentRepository = assessmentRepository;
        this.assessmentService = assessmentService;
        this.attackService = attackService;
        this.objectMapper = objectMapper;
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
        model.addAttribute("savedAssessmentId", assessmentId);
        model.addAttribute("assessmentComplete", assessment.getControlStatusMapping() != null && !assessment.getControlStatusMapping().isEmpty());

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
        model.addAttribute("assessmentComplete", existing.get().getControlStatusMapping() != null && !existing.get().getControlStatusMapping().isEmpty());
        return "assessment/mitreAssessment";
    }

    /**
     * Renders the marginal gains heatmap for the given assessment, showing how much additional
     * technique coverage each control could contribute by raising whichever single dimension
     * (scope or maturity) yields the greater gain. Controls are grouped by topic; each control's
     * total gain is the sum of its per-technique marginal gains passed to the view as
     * {@code groupedGains}, and {@code adviceMap} carries the recommended dimension per control.
     */
    @GetMapping("/assessment/{assessmentId}/marginal-gains")
    public String viewMarginalGains(Model model, @PathVariable UUID assessmentId) {
        Optional<Assessment> existing = assessmentRepository.findById(assessmentId);
        if (existing.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assessment not found");
        }
        AssessmentDTO dto = new AssessmentDTO(existing.get());

        Map<Control, MarginalGain> marginalGains = assessmentService.calculateMarginalGains(dto);

        // Read the precomputed total gain per control
        Map<Control, Double> totalGains = new LinkedHashMap<>();
        for (Map.Entry<Control, MarginalGain> entry : marginalGains.entrySet()) {
            totalGains.put(entry.getKey(), entry.getValue().totalGain());
        }

        // Sort controls by descending gain; position in this list is the global rank
        List<Map.Entry<Control, Double>> sortedEntries = new ArrayList<>(totalGains.entrySet());
        sortedEntries.sort(new Comparator<Map.Entry<Control, Double>>() {
            @Override
            public int compare(Map.Entry<Control, Double> a, Map.Entry<Control, Double> b) {
                return b.getValue().compareTo(a.getValue());
            }
        });

        List<Control> ranked = new ArrayList<>();
        for (Map.Entry<Control, Double> entry : sortedEntries) {
            ranked.add(entry.getKey());
        }

        Map<UUID, Integer> rankMap = new LinkedHashMap<>();
        for (int i = 0; i < ranked.size(); i++) {
            rankMap.put(ranked.get(i).getId(), i + 1);
        }

        // Group by topic in rank order so controls within each topic section are also sorted
        Map<Topic, Map<Control, Double>> groupedGains = new LinkedHashMap<>();
        for (Control control : ranked) {
            Topic topic = control.getTopic();
            if (!groupedGains.containsKey(topic)) {
                groupedGains.put(topic, new LinkedHashMap<>());
            }
            groupedGains.get(topic).put(control, totalGains.get(control));
        }

        Map<Control, Double> topFive = new LinkedHashMap<>();
        for (int i = 0; i < ranked.size() && i < 5; i++) {
            topFive.put(ranked.get(i), totalGains.get(ranked.get(i)));
        }

        // Advice badge text (which dimension to raise) keyed by control id, for the cards
        Map<UUID, String> adviceMap = new LinkedHashMap<>();
        for (Map.Entry<Control, MarginalGain> entry : marginalGains.entrySet()) {
            adviceMap.put(entry.getKey().getId(), entry.getValue().advice().getAdvice());
        }

        // Build a JSON blob for the control detail modal so the template needs no th:inline="javascript"
        Map<String, Object> controlModalData = new LinkedHashMap<>();
        for (Map.Entry<Control, MarginalGain> cEntry : marginalGains.entrySet()) {
            Control control = cEntry.getKey();
            MarginalGain gain = cEntry.getValue();
            List<Map<String, Object>> techniques = new ArrayList<>();
            for (Map.Entry<Technique, Double> tEntry : gain.techniqueGains().entrySet()) {
                Map<String, Object> tech = new LinkedHashMap<>();
                tech.put("mitreId", tEntry.getKey().getMitreId());
                tech.put("name", tEntry.getKey().getName());
                tech.put("gain", tEntry.getValue());
                techniques.add(tech);
            }
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("name", control.getName());
            info.put("description", control.getDescription() != null ? control.getDescription() : "");
            info.put("code", control.getCode());
            info.put("topicName", control.getTopic().getName());
            info.put("topicColor", control.getTopic().getColor() != null ? control.getTopic().getColor() : "#6c757d");
            info.put("advice", gain.advice().getAdvice());
            info.put("techniques", techniques);
            controlModalData.put(control.getId().toString(), info);
        }
        String controlDataJson;
        try {
            controlDataJson = objectMapper.writeValueAsString(controlModalData);
        } catch (JsonProcessingException e) {
            controlDataJson = "{}";
        }

        model.addAttribute("assessment", dto);
        model.addAttribute("groupedGains", groupedGains);
        model.addAttribute("rankMap", rankMap);
        model.addAttribute("adviceMap", adviceMap);
        model.addAttribute("topFive", topFive);
        model.addAttribute("controlDataJson", controlDataJson);
        model.addAttribute("assessmentComplete", existing.get().getControlStatusMapping() != null && !existing.get().getControlStatusMapping().isEmpty());
        return "assessment/marginalGains";
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
            return "redirect:/assessment/" + assessmentId + "/coverage";
        }
        return "redirect:/assessment/" + assessmentId + "/prioritize";
    }
}
