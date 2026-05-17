package com.czintercity.icsec_app.assessment.dto;

import com.czintercity.icsec_app.assessment.model.TacticAssessmentResult;
import com.czintercity.icsec_app.assessment.model.TechniqueAssessmentResult;
import com.czintercity.icsec_app.assessment.entity.Assessment;
import com.czintercity.icsec_app.attack.entity.Tactic;

import java.util.Map;

/**
 * Carries the results of a full MITRE ATT&amp;CK coverage calculation for a single
 * {@link com.czintercity.icsec_app.assessment.entity.Assessment}.
 * <p>
 * Coverage scores are organised as a two-level map: tactic → technique →
 * {@link TechniqueAssessmentResult}, where
 * each score tracks the residual failure probability per
 * {@link com.czintercity.icsec_app.relationships.techniqueCoverage.CoverageType}.
 *
 * @see com.czintercity.icsec_app.assessment.service.CoverageCalculationService
 */
public class AssessmentResultDTO {
    private Assessment assessment;
    private Map<Tactic, TacticAssessmentResult> coverageScores;

    public void setAssessment(Assessment assessment) { this.assessment = assessment; }
    public void setCoverageScores(Map<Tactic, TacticAssessmentResult> coverageScores) { this.coverageScores = coverageScores; }

    public Assessment getAssessment() { return this.assessment; }
    public Map<Tactic, TacticAssessmentResult> getCoverageScores() { return this.coverageScores; }
}

