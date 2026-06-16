package com.czintercity.icsec_app.assessment.controller;

import com.czintercity.icsec_app.assessment.dto.AssessmentDTO;
import com.czintercity.icsec_app.assessment.dto.AssessmentResultDTO;
import com.czintercity.icsec_app.assessment.dto.TechniqueAssessmentDetailDTO;
import com.czintercity.icsec_app.assessment.model.TacticAssessmentResult;
import com.czintercity.icsec_app.assessment.model.TechniqueAssessmentResult;
import com.czintercity.icsec_app.assessment.entity.Assessment;
import com.czintercity.icsec_app.assessment.repository.AssessmentRepository;
import com.czintercity.icsec_app.assessment.service.AssessmentService;
import com.czintercity.icsec_app.assessment.service.CoverageCalculationService;
import com.czintercity.icsec_app.attack.service.AttackService;
import com.czintercity.icsec_app.attack.entity.Technique;
import com.czintercity.icsec_app.attack.repository.TechniqueRepository;
import com.czintercity.icsec_app.relationships.techniqueCoverage.CoverageType;
import com.czintercity.icsec_app.relationships.techniqueCoverage.entity.TechniqueCoverage;
import com.czintercity.icsec_app.relationships.techniqueCoverage.repository.TechniqueCoverageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Handles HTTP requests for the MITRE ATT&amp;CK coverage heatmap and technique detail views.
 * Coverage scores are calculated per {@link com.czintercity.icsec_app.relationships.techniqueCoverage.CoverageType}
 * and translated into background tint colours for the heatmap cards.
 */
@Controller
public class CoverageController {

    private final AssessmentRepository assessmentRepository;
    private final AssessmentService assessmentService;
    private final AttackService attackService;
    private final CoverageCalculationService coverageCalculationService;
    private final TechniqueRepository techniqueRepository;
    private final TechniqueCoverageRepository techniqueCoverageRepository;

    public CoverageController(AssessmentRepository assessmentRepository, AssessmentService assessmentService,
                              AttackService attackService, CoverageCalculationService coverageCalculationService,
                              TechniqueRepository techniqueRepository,
                              TechniqueCoverageRepository techniqueCoverageRepository) {
        this.assessmentRepository = assessmentRepository;
        this.assessmentService = assessmentService;
        this.attackService = attackService;
        this.coverageCalculationService = coverageCalculationService;
        this.techniqueRepository = techniqueRepository;
        this.techniqueCoverageRepository = techniqueCoverageRepository;
    }

    /**
     * Renders the full coverage heatmap page for the given assessment.
     * Defaults to {@code PREVENTATIVE} coverage type when none is specified.
     *
     * @param coverageType name of the {@link com.czintercity.icsec_app.relationships.techniqueCoverage.CoverageType}
     *                     enum constant to use for scoring and colouring
     */
    @GetMapping("/assessment/{assessmentId}/coverage")
    public String assessmentResult(Model model, @PathVariable UUID assessmentId,
                                   @RequestParam(defaultValue = "PREVENTATIVE") String coverageType) {
        Optional<Assessment> existing = assessmentRepository.findById(assessmentId);
        if (existing.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assessment not found");
        }
        Assessment assessment = existing.get();

        CoverageType selectedType = CoverageType.valueOf(coverageType);
        AssessmentResultDTO coverageDTO = coverageCalculationService.calculateMitreCoverage(assessment);
        Map<UUID, String> techniqueColors = buildColorMap(coverageDTO, selectedType);
        Map<UUID, Double> techniqueScores = buildScoreMap(coverageDTO, selectedType);
        Map<UUID, Double> techniqueOptimumScores = buildOptimumScoreMap(coverageDTO, selectedType);
        Map<UUID, String> techniquePriorityColors = buildPriorityColorMap(coverageDTO, selectedType);
        Map<UUID, Double> techniquePriorityScores = buildPriorityScoreMap(coverageDTO, selectedType);

        model.addAttribute("assessment", new AssessmentDTO(assessment));
        model.addAttribute("assessmentComplete", assessment.getControlStatusMapping() != null && !assessment.getControlStatusMapping().isEmpty());
        model.addAttribute("coverageTypes", CoverageType.values());
        model.addAttribute("selectedCoverageType", selectedType);
        model.addAttribute("coverageTypeHex", selectedType.getHexColor());
        model.addAttribute("tacticsMap", attackService.getTacticsWithTechniques());
        model.addAttribute("techniqueColors", techniqueColors);
        model.addAttribute("techniqueScores", techniqueScores);
        model.addAttribute("techniqueOptimumScores", techniqueOptimumScores);
        model.addAttribute("techniquePriorityColors", techniquePriorityColors);
        model.addAttribute("techniquePriorityScores", techniquePriorityScores);
        model.addAttribute("topPriorityTechniques", buildTopPriorityTechniques(coverageDTO, selectedType));

        return "assessment/assessmentResult";
    }

    /**
     * Returns the heatmap grid fragment for HTMX partial updates.
     * Called when the coverage-type dropdown changes on the result page;
     * replaces only the technique card grid without a full page reload.
     *
     * @param coverageType name of the {@link com.czintercity.icsec_app.relationships.techniqueCoverage.CoverageType}
     *                     enum constant selected by the user
     */
    @GetMapping("/assessment/{assessmentId}/coverage/heatmap")
    public String assessmentResultHeatmap(Model model, @PathVariable UUID assessmentId,
                                          @RequestParam(defaultValue = "PREVENTATIVE") String coverageType) {
        Optional<Assessment> existing = assessmentRepository.findById(assessmentId);
        if (existing.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assessment not found");
        }
        Assessment assessment = existing.get();

        CoverageType selectedType = CoverageType.valueOf(coverageType);
        AssessmentResultDTO coverageDTO = coverageCalculationService.calculateMitreCoverage(assessment);
        Map<UUID, String> techniqueColors = buildColorMap(coverageDTO, selectedType);
        Map<UUID, Double> techniqueScores = buildScoreMap(coverageDTO, selectedType);
        Map<UUID, Double> techniqueOptimumScores = buildOptimumScoreMap(coverageDTO, selectedType);
        Map<UUID, String> techniquePriorityColors = buildPriorityColorMap(coverageDTO, selectedType);
        Map<UUID, Double> techniquePriorityScores = buildPriorityScoreMap(coverageDTO, selectedType);

        model.addAttribute("assessment", new AssessmentDTO(assessment));
        model.addAttribute("selectedCoverageType", selectedType);
        model.addAttribute("coverageTypeHex", selectedType.getHexColor());
        model.addAttribute("tacticsMap", attackService.getTacticsWithTechniques());
        model.addAttribute("techniqueColors", techniqueColors);
        model.addAttribute("techniqueScores", techniqueScores);
        model.addAttribute("techniqueOptimumScores", techniqueOptimumScores);
        model.addAttribute("techniquePriorityColors", techniquePriorityColors);
        model.addAttribute("techniquePriorityScores", techniquePriorityScores);
        model.addAttribute("topPriorityTechniques", buildTopPriorityTechniques(coverageDTO, selectedType));

        return "assessment/assessmentResult :: heatmap";
    }

    /**
     * Returns the technique detail modal fragment for HTMX partial updates.
     * Loads all {@link com.czintercity.icsec_app.relationships.techniqueCoverage.entity.TechniqueCoverage}
     * records for the technique, groups them by coverage type, and computes the maximum rating per type.
     */
    @GetMapping("/technique/{techniqueId}/coverage-detail")
    public String techniqueDetail(Model model, @PathVariable UUID techniqueId) {
        Optional<Technique> techniqueOpt = techniqueRepository.findById(techniqueId);
        if (techniqueOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Technique not found");
        }
        Technique technique = techniqueOpt.get();

        List<TechniqueCoverage> allCoverage = techniqueCoverageRepository.findByTechnique(technique);

        Map<CoverageType, List<TechniqueCoverage>> coverageByType = new LinkedHashMap<>();
        Map<CoverageType, Short> maxRatings = new LinkedHashMap<>();

        for (TechniqueCoverage coverage : allCoverage) {
            CoverageType type = coverage.getCoverageType();
            if (!coverageByType.containsKey(type)) {
                coverageByType.put(type, new ArrayList<>());
                maxRatings.put(type, (short) 0);
            }
            coverageByType.get(type).add(coverage);
            if (coverage.getCoverageRating() > maxRatings.get(type)) {
                maxRatings.put(type, coverage.getCoverageRating());
            }
        }

        model.addAttribute("technique", technique);
        model.addAttribute("coverageByType", coverageByType);
        model.addAttribute("maxRatings", maxRatings);

        return "fragments/techniqueDetail :: techniqueDetail";
    }

    /**
     * Returns the assessment-aware technique detail modal fragment.
     * Controls covering the technique are split into those already active in the assessment
     * (shown with effective ratings) and those not yet included (shown with raw ratings).
     */
    @GetMapping("/assessment/{assessmentId}/technique/{techniqueId}/coverage-detail")
    public String techniqueAssessmentDetail(Model model, @PathVariable UUID assessmentId,
                                            @PathVariable UUID techniqueId) {
        Optional<Assessment> assessmentOpt = assessmentRepository.findById(assessmentId);
        if (assessmentOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assessment not found");
        }
        Optional<Technique> techniqueOpt = techniqueRepository.findById(techniqueId);
        if (techniqueOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Technique not found");
        }

        TechniqueAssessmentDetailDTO detail = coverageCalculationService
                .getTechniqueAssessmentDetail(assessmentOpt.get(), techniqueOpt.get());

        model.addAttribute("technique", techniqueOpt.get());
        model.addAttribute("detail", detail);

        return "fragments/techniqueDetail :: techniqueAssessmentDetail";
    }

    /**
     * Builds a map from technique ID to a CSS tint string for the given coverage type.
     * Colour intensity reflects the effective coverage score on a fixed 0–5 scale.
     */
    private Map<UUID, String> buildColorMap(AssessmentResultDTO coverageDTO, CoverageType type) {
        Map<UUID, String> colors = new HashMap<>();
        for (TacticAssessmentResult tacticScore : coverageDTO.getCoverageScores().values()) {
            for (Map.Entry<Technique, TechniqueAssessmentResult> entry : tacticScore.getTechniqueAssessmentResults().entrySet()) {
                UUID techniqueId = entry.getKey().getId();
                double score = entry.getValue().getAssessmentResults().get(type).getEffectiveCoverageScore();
                colors.put(techniqueId, type.tintColor(score));
            }
        }
        return colors;
    }

    /** Builds a map from technique ID to its effective coverage score (0–5) for the given coverage type. */
    private Map<UUID, Double> buildScoreMap(AssessmentResultDTO coverageDTO, CoverageType type) {
        Map<UUID, Double> scores = new HashMap<>();
        for (TacticAssessmentResult tacticScore : coverageDTO.getCoverageScores().values()) {
            for (Map.Entry<Technique, TechniqueAssessmentResult> entry : tacticScore.getTechniqueAssessmentResults().entrySet()) {
                UUID techniqueId = entry.getKey().getId();
                scores.put(techniqueId, entry.getValue().getAssessmentResults().get(type).getEffectiveCoverageScore());
            }
        }
        return scores;
    }

    /** Builds a map from technique ID to its optimum coverage score (0–5) for the given coverage type. */
    private Map<UUID, Double> buildOptimumScoreMap(AssessmentResultDTO coverageDTO, CoverageType type) {
        Map<UUID, Double> scores = new HashMap<>();
        for (TacticAssessmentResult tacticScore : coverageDTO.getCoverageScores().values()) {
            for (Map.Entry<Technique, TechniqueAssessmentResult> entry : tacticScore.getTechniqueAssessmentResults().entrySet()) {
                UUID techniqueId = entry.getKey().getId();
                scores.put(techniqueId, entry.getValue().getAssessmentResults().get(type).getOptimumCoverageScore());
            }
        }
        return scores;
    }

    /**
     * Builds a map from technique ID to a CSS tint string representing weighted priority for the given coverage type.
     * Colour intensity is normalised so the technique with the highest weighted priority renders at full saturation.
     */
    private Map<UUID, String> buildPriorityColorMap(AssessmentResultDTO coverageDTO, CoverageType type) {
        Map<UUID, Double> priorities = new HashMap<>();
        double maxPriority = 0.0;

        for (TacticAssessmentResult tacticScore : coverageDTO.getCoverageScores().values()) {
            for (Map.Entry<Technique, TechniqueAssessmentResult> entry : tacticScore.getTechniqueAssessmentResults().entrySet()) {
                double p = entry.getValue().getAssessmentResults().get(type).getWeightedPriority();
                priorities.put(entry.getKey().getId(), p);
                if (p > maxPriority) maxPriority = p;
            }
        }

        Map<UUID, String> colors = new HashMap<>();
        double max = maxPriority;
        for (Map.Entry<UUID, Double> entry : priorities.entrySet()) {
            double normalised = max > 0 ? (entry.getValue() / max) * 5.0 : 0.0;
            colors.put(entry.getKey(), type.tintColor(normalised));
        }
        return colors;
    }

    /** Builds a map from technique ID to its weighted priority score (0–5) for the given coverage type. */
    private Map<UUID, Double> buildPriorityScoreMap(AssessmentResultDTO coverageDTO, CoverageType type) {
        Map<UUID, Double> scores = new HashMap<>();
        for (TacticAssessmentResult tacticScore : coverageDTO.getCoverageScores().values()) {
            for (Map.Entry<Technique, TechniqueAssessmentResult> entry : tacticScore.getTechniqueAssessmentResults().entrySet()) {
                UUID techniqueId = entry.getKey().getId();
                double priority = entry.getValue().getAssessmentResults().get(type).getWeightedPriority();
                scores.put(techniqueId, priority);
            }
        }
        return scores;
    }

    /**
     * Returns the five techniques with the highest weighted priority for the given coverage type,
     * in descending order. Techniques with no weighted priority (zero) are excluded, so the list may
     * hold fewer than five entries.
     */
    private List<Technique> buildTopPriorityTechniques(AssessmentResultDTO coverageDTO, CoverageType type) {
        List<Map.Entry<Technique, Double>> ranked = new ArrayList<>();
        for (TacticAssessmentResult tacticScore : coverageDTO.getCoverageScores().values()) {
            for (Map.Entry<Technique, TechniqueAssessmentResult> entry : tacticScore.getTechniqueAssessmentResults().entrySet()) {
                double priority = entry.getValue().getAssessmentResults().get(type).getWeightedPriority();
                if (priority > 0.0) {
                    ranked.add(Map.entry(entry.getKey(), priority));
                }
            }
        }
        ranked.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        List<Technique> top = new ArrayList<>();
        for (int i = 0; i < ranked.size() && i < 5; i++) {
            top.add(ranked.get(i).getKey());
        }
        return top;
    }
}