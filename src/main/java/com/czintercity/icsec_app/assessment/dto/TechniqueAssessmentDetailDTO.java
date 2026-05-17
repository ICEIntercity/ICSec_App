package com.czintercity.icsec_app.assessment.dto;

import com.czintercity.icsec_app.relationships.techniqueCoverage.CoverageType;

import java.util.List;
import java.util.Map;

/**
 * Carries the per-technique coverage breakdown for an assessment detail view.
 * Controls are split into two groups — those already active in the assessment
 * (with effective ratings) and those not yet included (raw potential ratings) —
 * and within each group are further grouped by {@link CoverageType}.
 */
public class TechniqueAssessmentDetailDTO {

    private final Map<CoverageType, List<ControlCoverageRowDTO>> existingByType;
    private final Map<CoverageType, List<ControlCoverageRowDTO>> additionalByType;

    public TechniqueAssessmentDetailDTO(
            Map<CoverageType, List<ControlCoverageRowDTO>> existingByType,
            Map<CoverageType, List<ControlCoverageRowDTO>> additionalByType) {
        this.existingByType = existingByType;
        this.additionalByType = additionalByType;
    }

    public Map<CoverageType, List<ControlCoverageRowDTO>> getExistingByType() { return existingByType; }
    public Map<CoverageType, List<ControlCoverageRowDTO>> getAdditionalByType() { return additionalByType; }
}