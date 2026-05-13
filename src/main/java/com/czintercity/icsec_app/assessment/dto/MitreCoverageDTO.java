package com.czintercity.icsec_app.assessment.dto;

import com.czintercity.icsec_app.assessment.dto.util.TacticCoverageScore;
import com.czintercity.icsec_app.assessment.entity.Assessment;
import com.czintercity.icsec_app.attack.entity.Tactic;

import java.util.Map;

public class MitreCoverageDTO {
    private Assessment assessment;
    private Map<Tactic, TacticCoverageScore> coverageScores;

    public void setAssessment(Assessment assessment) { this.assessment = assessment; }
    public void setCoverageScores(Map<Tactic, TacticCoverageScore> coverageScores) { this.coverageScores = coverageScores; }

    public Assessment getAssessment() { return this.assessment; }
    public Map<Tactic, TacticCoverageScore> getCoverageScores() { return this.coverageScores; }
}

