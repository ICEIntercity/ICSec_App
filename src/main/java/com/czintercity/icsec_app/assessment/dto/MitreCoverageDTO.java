package com.czintercity.icsec_app.assessment.dto;

import com.czintercity.icsec_app.assessment.dto.util.TacticCoverageScore;
import com.czintercity.icsec_app.assessment.entity.Assessment;
import com.czintercity.icsec_app.attack.entity.Tactic;

import java.util.Map;

/**
 * Carries the results of a full MITRE ATT&amp;CK coverage calculation for a single
 * {@link com.czintercity.icsec_app.assessment.entity.Assessment}.
 * <p>
 * Coverage scores are organised as a two-level map: tactic → technique →
 * {@link com.czintercity.icsec_app.assessment.dto.util.TechniqueCoverageScore}, where
 * each score tracks the residual failure probability per
 * {@link com.czintercity.icsec_app.relationships.techniqueCoverage.CoverageType}.
 *
 * @see com.czintercity.icsec_app.assessment.service.CoverageCalculationService
 */
public class MitreCoverageDTO {
    private Assessment assessment;
    private Map<Tactic, TacticCoverageScore> coverageScores;

    public void setAssessment(Assessment assessment) { this.assessment = assessment; }
    public void setCoverageScores(Map<Tactic, TacticCoverageScore> coverageScores) { this.coverageScores = coverageScores; }

    public Assessment getAssessment() { return this.assessment; }
    public Map<Tactic, TacticCoverageScore> getCoverageScores() { return this.coverageScores; }
}

