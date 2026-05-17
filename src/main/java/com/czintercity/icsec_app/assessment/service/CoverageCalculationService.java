package com.czintercity.icsec_app.assessment.service;

import com.czintercity.icsec_app.assessment.dto.AssessmentResultDTO;
import com.czintercity.icsec_app.assessment.dto.ControlCoverageRowDTO;
import com.czintercity.icsec_app.assessment.dto.TechniqueAssessmentDetailDTO;
import com.czintercity.icsec_app.assessment.model.AssessmentValues;
import com.czintercity.icsec_app.assessment.model.TacticAssessmentResult;
import com.czintercity.icsec_app.assessment.model.TechniqueAssessmentResult;
import com.czintercity.icsec_app.assessment.entity.Assessment;
import com.czintercity.icsec_app.assessment.entity.ControlStatus;
import com.czintercity.icsec_app.assessment.exception.BlankAssessmentException;
import com.czintercity.icsec_app.attack.entity.Tactic;
import com.czintercity.icsec_app.attack.entity.Technique;
import com.czintercity.icsec_app.attack.repository.TacticRepository;
import com.czintercity.icsec_app.controls.entity.Control;
import com.czintercity.icsec_app.relationships.techniqueCoverage.CoverageType;
import com.czintercity.icsec_app.relationships.techniqueCoverage.entity.TechniqueCoverage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Calculates MITRE ATT&amp;CK coverage scores for a given {@link Assessment}.
 * <p>
 * For every technique in the framework, each active control reduces the residual
 * failure probability for the coverage types it addresses. The effective contribution
 * of a control is weighted by its scope and maturity scores:
 * <pre>
 *   effectiveCoverageScore = (scope^0.65 × maturity^0.35 / 5) × coverageRating
 *   effectiveFailureProbability = max(0, 1 − effectiveCoverageScore / 5)
 * </pre>
 * Multiple controls compound multiplicatively: each one further reduces the
 * probability that remains after the previous controls have been applied.
 */
@Service
public class CoverageCalculationService {

    private final TacticRepository tacticRepository;

    public CoverageCalculationService(TacticRepository tacticRepository) {
        this.tacticRepository = tacticRepository;
    }

    /**
     * Computes a full MITRE ATT&amp;CK coverage assessment and returns the results as a
     * {@link AssessmentResultDTO}.
     * <p>
     * All tactics and techniques are initialised with a failure probability of {@code 1.0}.
     * Each {@link ControlStatus} in the assessment is then applied: for every
     * {@link TechniqueCoverage} relationship on the control, the residual failure
     * probability for the corresponding technique and coverage type is multiplied by
     * the control's effective failure probability.
     *
     * @param assessment the assessment whose control statuses drive the calculation
     * @return a {@link AssessmentResultDTO} containing per-technique scores for every tactic
     * @throws BlankAssessmentException if the assessment or its control status mapping is {@code null}
     */
    @Transactional
    public AssessmentResultDTO calculateMitreCoverage(Assessment assessment) {
        if (assessment == null || assessment.getControlStatusMapping() == null) {
            throw new BlankAssessmentException("Missing assessment or control status mapping.");
        }

        // Initialize a TacticAssessmentResult per tactic, with a blank TechniqueCoverageScore per technique
        Map<Tactic, TacticAssessmentResult> tacticAssessmentResults = new HashMap<>();
        for (Tactic tactic : tacticRepository.findAll()) {
            TacticAssessmentResult tacticResult = new TacticAssessmentResult();
            for (Technique technique : tactic.getTechniques()) {
                // Initialize a blank technique assessment
                TechniqueAssessmentResult techniqueResult = new TechniqueAssessmentResult();
                Short techniquePriority = assessment.getTechniquePriorities().getOrDefault(technique, (short) 0);

                // Initialize coverages for each type of coverage
                for (CoverageType coverageType : CoverageType.values()) {

                    // Load the result for coverage type (for easier access)
                    AssessmentValues typeValues = techniqueResult.getAssessmentResults().get(coverageType);

                    // Set weight for assessment
                    typeValues.setPriority(techniquePriority);

                    // Prepare for calculating the "optimum state" if all possible controls are applied.
                    // Start with 1.0 value because if no controls are applied, p(Failure) = 1
                    double optimumFailureProbability = 1.0;

                    // Calculate optimum state
                    for (TechniqueCoverage coverage : technique.getCoverages()) {
                        if(coverage.getCoverageType() == coverageType) {
                            optimumFailureProbability = optimumFailureProbability * ((double) (5 - coverage.getCoverageRating()) / 5);
                        }
                    }
                    typeValues.setOptimumFailureProbability(optimumFailureProbability);
                }
                tacticResult.getTechniqueAssessmentResults().put(technique, techniqueResult);
            }
            tacticAssessmentResults.put(tactic, tacticResult);
        }

        // Multiply each technique's failure probability by the control's effective reduction
        for (ControlStatus controlStatus : assessment.getControlStatusMapping()) {
            Control control = controlStatus.getControl();
            Short scope = controlStatus.getCoverageScope();
            Short maturity = controlStatus.getCoverageMaturity();

            for (TechniqueCoverage coverage : control.getTechniqueCoverage()) {
                Technique technique = coverage.getTechnique();
                Short coverageRating = coverage.getCoverageRating();
                CoverageType coverageType = coverage.getCoverageType();

                // Effective coverage formula - exponents weight scope more heavily than maturity
                double effectiveCoverageScore = (Math.pow(scope, 0.65) * Math.pow(maturity, 0.35) / 5) * coverageRating;
                double effectiveFailureProbability = Math.max(0.0, 1 - (effectiveCoverageScore / 5));

                for (Tactic tactic : technique.getTactics()) {
                    TechniqueAssessmentResult techniqueResult = tacticAssessmentResults.get(tactic).getTechniqueAssessmentResults().get(technique);
                    AssessmentValues values = techniqueResult.getAssessmentResults().get(coverageType);

                    // Recalculate failure probability (using parallel systems failure probability formula)
                    double currentFailureProbability = values.getEffectiveFailureProbability();
                    values.setEffectiveFailureProbability(currentFailureProbability * effectiveFailureProbability);
                }
            }
        }

        AssessmentResultDTO dto = new AssessmentResultDTO();
        dto.setAssessment(assessment);
        dto.setCoverageScores(tacticAssessmentResults);
        return dto;
    }

    /**
     * Builds a {@link TechniqueAssessmentDetailDTO} for a single technique in the context of
     * an assessment. Controls that cover the technique are split into two groups:
     * <ul>
     *   <li><em>Existing</em> — controls with a non-blank {@link ControlStatus} in the assessment,
     *       shown with their maturity/scope-adjusted effective rating.</li>
     *   <li><em>Additional</em> — controls not yet active in the assessment,
     *       shown with their raw coverage rating as the potential gain.</li>
     * </ul>
     * Within each group the rows are further grouped by {@link CoverageType}.
     *
     * @param assessment the assessment providing the active control statuses
     * @param technique  the technique whose coverage rows are being inspected
     * @return a {@link TechniqueAssessmentDetailDTO} ready for the detail modal fragment
     */
    @Transactional
    public TechniqueAssessmentDetailDTO getTechniqueAssessmentDetail(Assessment assessment, Technique technique) {
        Map<UUID, ControlStatus> statusMap = new HashMap<>();
        for (ControlStatus cs : assessment.getControlStatusMapping()) {
            statusMap.put(cs.getControl().getId(), cs);
        }

        Map<CoverageType, List<ControlCoverageRowDTO>> existingByType = new LinkedHashMap<>();
        Map<CoverageType, List<ControlCoverageRowDTO>> additionalByType = new LinkedHashMap<>();

        for (TechniqueCoverage coverage : technique.getCoverages()) {
            CoverageType type = coverage.getCoverageType();
            ControlStatus status = statusMap.get(coverage.getControl().getId());

            if (status != null && !status.isBlank()) {
                double effectiveRating = (Math.pow(status.getCoverageScope(), 0.65)
                        * Math.pow(status.getCoverageMaturity(), 0.35) / 5)
                        * coverage.getCoverageRating();
                if (!existingByType.containsKey(type)) {
                    existingByType.put(type, new ArrayList<>());
                }
                existingByType.get(type).add(new ControlCoverageRowDTO(coverage, effectiveRating));
            } else {
                if (!additionalByType.containsKey(type)) {
                    additionalByType.put(type, new ArrayList<>());
                }
                additionalByType.get(type).add(new ControlCoverageRowDTO(coverage, null));
            }
        }

        return new TechniqueAssessmentDetailDTO(existingByType, additionalByType);
    }
}