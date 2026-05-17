package com.czintercity.icsec_app.assessment.dto;

import com.czintercity.icsec_app.relationships.techniqueCoverage.entity.TechniqueCoverage;

/**
 * A single row in a technique's assessment detail view, pairing a {@link TechniqueCoverage}
 * record with the effective (maturity/scope-adjusted) rating for controls already active in
 * the assessment. The effective rating is {@code null} for controls not yet in the assessment.
 */
public class ControlCoverageRowDTO {

    private final TechniqueCoverage coverage;
    private final Double effectiveRating;

    public ControlCoverageRowDTO(TechniqueCoverage coverage, Double effectiveRating) {
        this.coverage = coverage;
        this.effectiveRating = effectiveRating;
    }

    public TechniqueCoverage getCoverage() { return coverage; }
    public Double getEffectiveRating() { return effectiveRating; }
}