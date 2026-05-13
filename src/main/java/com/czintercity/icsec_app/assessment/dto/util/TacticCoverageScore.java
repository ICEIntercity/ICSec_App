package com.czintercity.icsec_app.assessment.dto.util;

import com.czintercity.icsec_app.attack.entity.Technique;

import java.util.HashMap;
import java.util.Map;

/**
 * Intermediate calculation structure holding a coverage score for each
 * {@link com.czintercity.icsec_app.attack.entity.Technique} belonging to a single tactic.
 * Populated by {@link com.czintercity.icsec_app.assessment.service.CoverageCalculationService}
 * and consumed by {@link com.czintercity.icsec_app.assessment.dto.MitreCoverageDTO}.
 */
public class TacticCoverageScore {
    public Map<Technique, TechniqueCoverageScore> techniqueCoverageScores;

    public TacticCoverageScore(){
        techniqueCoverageScores = new HashMap<>();
    }
}
