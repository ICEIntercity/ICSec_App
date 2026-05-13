package com.czintercity.icsec_app.assessment.dto.util;

import com.czintercity.icsec_app.relationships.techniqueCoverage.CoverageType;

import java.util.HashMap;
import java.util.Map;

public class TechniqueCoverageScore {
    public Map<CoverageType, Double> typeFailureProbabilities;

    public TechniqueCoverageScore(){
        typeFailureProbabilities = new HashMap<>();
        for (CoverageType coverageType : CoverageType.values()) {
            typeFailureProbabilities.put(coverageType, 1.0);
        }
    }

    public Double getCoverageScore(CoverageType type){
        if(!typeFailureProbabilities.containsKey(type)){
            throw new IllegalArgumentException("Invalid coverage type: " + type);
        }
        return 5 - typeFailureProbabilities.get(type);
    }
}
