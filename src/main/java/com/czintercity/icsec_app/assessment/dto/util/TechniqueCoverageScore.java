package com.czintercity.icsec_app.assessment.dto.util;

import com.czintercity.icsec_app.relationships.techniqueCoverage.CoverageType;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracks the residual failure probability for a single technique, broken down by
 * {@link com.czintercity.icsec_app.relationships.techniqueCoverage.CoverageType}.
 * <p>
 * Each type begins at a failure probability of {@code 1.0} (fully uncovered) and is
 * multiplied down as effective control coverage is applied. The resulting coverage
 * score is derived as {@code 5 − (failureProbability × 5)}, yielding a value in [0, 5].
 */
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
        return 5 - (typeFailureProbabilities.get(type) * 5);
    }
}
