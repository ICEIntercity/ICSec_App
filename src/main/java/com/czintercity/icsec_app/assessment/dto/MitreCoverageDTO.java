package com.czintercity.icsec_app.assessment.dto;

import com.czintercity.icsec_app.assessment.dto.util.TechniqueScore;
import com.czintercity.icsec_app.assessment.entity.Assessment;
import com.czintercity.icsec_app.attack.entity.Tactic;

import java.util.Map;

public class MitreCoverageDTO {
    private Assessment assessment;
    private Map<Tactic, TechniqueScore> techniqueCoverageScore;

    void setAssessment(Assessment assessment) { this.assessment = assessment; }
    void setTechniqueCoverageScore(Map<Tactic, TechniqueScore> techniqueCoverageScore) { this.techniqueCoverageScore = techniqueCoverageScore; }

    Map<Tactic, TechniqueScore> getTechniqueCoverageScore() { return this.techniqueCoverageScore; }
    Assessment getAssessment() { return this.assessment; }
}

