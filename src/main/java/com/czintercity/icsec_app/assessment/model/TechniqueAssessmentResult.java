package com.czintercity.icsec_app.assessment.model;

import com.czintercity.icsec_app.relationships.techniqueCoverage.CoverageType;

import java.util.EnumMap;

/**
 * Tracks the residual failure probability for a single technique, broken down by
 * {@link com.czintercity.icsec_app.relationships.techniqueCoverage.CoverageType}.
 * <p>
 * Each type begins at a failure probability of {@code 1.0} (fully uncovered) and is
 * multiplied down as effective control coverage is applied. The resulting coverage
 * score is derived as {@code 5 − (failureProbability × 5)}, yielding a value in [0, 5].
 */
public class TechniqueAssessmentResult {
    private EnumMap<CoverageType, AssessmentValues> assessmentResults;

    public TechniqueAssessmentResult(){
        assessmentResults = new EnumMap<>(CoverageType.class);
        for (CoverageType coverageType : CoverageType.values()) {
            assessmentResults.put(coverageType, new AssessmentValues(1d, 1d, 0d));
        }
    }

    public TechniqueAssessmentResult(EnumMap<CoverageType, AssessmentValues> assessmentResults) {
        this.assessmentResults = assessmentResults;
    }

    public EnumMap<CoverageType, AssessmentValues> getAssessmentResults() {
        return assessmentResults;
    }
}