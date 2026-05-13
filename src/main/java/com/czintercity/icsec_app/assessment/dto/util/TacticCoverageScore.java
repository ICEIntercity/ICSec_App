package com.czintercity.icsec_app.assessment.dto.util;

import com.czintercity.icsec_app.attack.entity.Technique;

import java.util.HashMap;
import java.util.Map;

public class TacticCoverageScore {
    public Map<Technique, TechniqueCoverageScore> techniqueCoverageScores;

    public TacticCoverageScore(){
        techniqueCoverageScores = new HashMap<>();
    }
}
