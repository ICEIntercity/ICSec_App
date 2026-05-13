package com.czintercity.icsec_app.assessment.dto;

import com.czintercity.icsec_app.assessment.entity.ControlStatus;

import java.util.UUID;

/**
 * Data transfer object representing the maturity and scope ratings assigned to a
 * {@link com.czintercity.icsec_app.controls.entity.Control} within a specific assessment.
 * <p>
 * Both scores use a 0–5 scale. An instance is considered blank when both scores are
 * absent or zero, allowing the service layer to skip persisting it.
 *
 * @see com.czintercity.icsec_app.assessment.entity.ControlStatus
 */
public class ControlStatusDTO {
    private UUID assessmentId;
    private UUID controlId;
    private Short coverageMaturity;
    private Short coverageScope;

    public UUID getControlId() { return controlId; }
    public void setControlId(UUID controlId) { this.controlId = controlId; }
    public Short getCoverageMaturity() { return coverageMaturity; }
    public void setCoverageMaturity(Short coverageMaturity) { this.coverageMaturity = coverageMaturity; }
    public Short getCoverageScope() { return coverageScope; }
    public void setCoverageScope(Short coverageScope) { this.coverageScope = coverageScope; }
    public UUID getAssessmentId() { return assessmentId; }
    public void setAssessmentId(UUID assessmentId) { this.assessmentId = assessmentId; }

    /**
     * Returns {@code true} when both {@code coverageMaturity} and {@code coverageScope}
     * are unset or zero, indicating no meaningful rating has been recorded.
     */
    public boolean isBlank() {
        return (coverageMaturity == null || coverageMaturity == 0)
            && (coverageScope == null || coverageScope == 0);
    }

    public ControlStatusDTO(){
        this.assessmentId = null;
        this.controlId = null;
        this.coverageMaturity = 0;
        this.coverageScope = 0;
    }

    /** Constructs a blank placeholder for the given assessment and control, with both scores unset. */
    public ControlStatusDTO(UUID assessmentId, UUID controlId){
        this.assessmentId = assessmentId;
        this.controlId = controlId;
    }

    /** Constructs a {@code ControlStatusDTO} from an existing {@link ControlStatus} entity. */
    public ControlStatusDTO(ControlStatus status) {
        this.assessmentId = status.getAssessment().getId();
        this.controlId = status.getControl().getId();
        this.coverageMaturity = status.getCoverageMaturity();
        this.coverageScope = status.getCoverageScope();
    }


}