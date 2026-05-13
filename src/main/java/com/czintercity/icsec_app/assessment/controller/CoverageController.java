package com.czintercity.icsec_app.assessment.controller;

import com.czintercity.icsec_app.assessment.dto.AssessmentDTO;
import com.czintercity.icsec_app.assessment.dto.MitreCoverageDTO;
import com.czintercity.icsec_app.assessment.dto.util.TacticCoverageScore;
import com.czintercity.icsec_app.assessment.dto.util.TechniqueCoverageScore;
import com.czintercity.icsec_app.assessment.entity.Assessment;
import com.czintercity.icsec_app.assessment.repository.AssessmentRepository;
import com.czintercity.icsec_app.assessment.service.AssessmentService;
import com.czintercity.icsec_app.assessment.service.CoverageCalculationService;
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
    private final CoverageCalculationService coverageCalculationService;
    private final TechniqueRepository techniqueRepository;
    private final TechniqueCoverageRepository techniqueCoverageRepository;

    public CoverageController(AssessmentRepository assessmentRepository, AssessmentService assessmentService,
                              CoverageCalculationService coverageCalculationService,
                              TechniqueRepository techniqueRepository,
                              TechniqueCoverageRepository techniqueCoverageRepository) {
        this.assessmentRepository = assessmentRepository;
        this.assessmentService = assessmentService;
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
    @GetMapping("/assessment/{assessmentId}/result")
    public String assessmentResult(Model model, @PathVariable UUID assessmentId,
                                   @RequestParam(defaultValue = "PREVENTATIVE") String coverageType) {
        Optional<Assessment> existing = assessmentRepository.findById(assessmentId);
        if (existing.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assessment not found");
        }
        Assessment assessment = existing.get();

        CoverageType selectedType = CoverageType.valueOf(coverageType);
        MitreCoverageDTO coverageDTO = coverageCalculationService.calculateMitreCoverage(assessment);
        Map<UUID, String> techniqueColors = buildColorMap(coverageDTO, selectedType);
        Map<UUID, Double> techniqueScores = buildScoreMap(coverageDTO, selectedType);

        model.addAttribute("assessment", new AssessmentDTO(assessment));
        model.addAttribute("coverageTypes", CoverageType.values());
        model.addAttribute("selectedCoverageType", selectedType);
        model.addAttribute("tacticsMap", assessmentService.getTacticsWithTechniques());
        model.addAttribute("techniqueColors", techniqueColors);
        model.addAttribute("techniqueScores", techniqueScores);

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
    @GetMapping("/assessment/{assessmentId}/result/heatmap")
    public String assessmentResultHeatmap(Model model, @PathVariable UUID assessmentId,
                                          @RequestParam(defaultValue = "PREVENTATIVE") String coverageType) {
        Optional<Assessment> existing = assessmentRepository.findById(assessmentId);
        if (existing.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assessment not found");
        }
        Assessment assessment = existing.get();

        CoverageType selectedType = CoverageType.valueOf(coverageType);
        MitreCoverageDTO coverageDTO = coverageCalculationService.calculateMitreCoverage(assessment);
        Map<UUID, String> techniqueColors = buildColorMap(coverageDTO, selectedType);
        Map<UUID, Double> techniqueScores = buildScoreMap(coverageDTO, selectedType);

        model.addAttribute("selectedCoverageType", selectedType);
        model.addAttribute("tacticsMap", assessmentService.getTacticsWithTechniques());
        model.addAttribute("techniqueColors", techniqueColors);
        model.addAttribute("techniqueScores", techniqueScores);

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
     * Builds a map from technique ID to a CSS {@code rgb()} tint string for the given coverage type.
     * The tint is interpolated linearly from white (score 0) to the type's base colour (score 5).
     */
    private Map<UUID, String> buildColorMap(MitreCoverageDTO coverageDTO, CoverageType type) {
        Map<UUID, String> colors = new HashMap<>();
        String hex = type.getHexColor();

        for (TacticCoverageScore tacticScore : coverageDTO.getCoverageScores().values()) {
            for (Map.Entry<Technique, TechniqueCoverageScore> entry : tacticScore.techniqueCoverageScores.entrySet()) {
                UUID techniqueId = entry.getKey().getId();
                double score = entry.getValue().getCoverageScore(type);
                colors.put(techniqueId, tintColor(hex, score));
            }
        }

        return colors;
    }

    /** Builds a map from technique ID to its computed coverage score (0–5) for the given coverage type. */
    private Map<UUID, Double> buildScoreMap(MitreCoverageDTO coverageDTO, CoverageType type) {
        Map<UUID, Double> scores = new HashMap<>();

        for (TacticCoverageScore tacticScore : coverageDTO.getCoverageScores().values()) {
            for (Map.Entry<Technique, TechniqueCoverageScore> entry : tacticScore.techniqueCoverageScores.entrySet()) {
                UUID techniqueId = entry.getKey().getId();
                double score = entry.getValue().getCoverageScore(type);
                scores.put(techniqueId, score);
            }
        }

        return scores;
    }

    /**
     * Computes a CSS {@code rgb()} colour that is a linear tint of {@code hexColor}.
     * A score of 0 produces white; a score of 5 produces the unmodified base colour.
     *
     * @param hexColor six-digit hex colour string prefixed with {@code #}
     * @param score    coverage score in the range [0, 5]
     * @return CSS colour string in the form {@code rgb(r,g,b)}
     */
    private static String tintColor(String hexColor, double score) {
        int r = Integer.parseInt(hexColor.substring(1, 3), 16);
        int g = Integer.parseInt(hexColor.substring(3, 5), 16);
        int b = Integer.parseInt(hexColor.substring(5, 7), 16);

        double t = score / 5.0;
        if (t < 0.0) t = 0.0;
        if (t > 1.0) t = 1.0;

        int tr = (int) Math.round(255 + (r - 255) * t);
        int tg = (int) Math.round(255 + (g - 255) * t);
        int tb = (int) Math.round(255 + (b - 255) * t);

        return String.format("rgb(%d,%d,%d)", tr, tg, tb);
    }
}