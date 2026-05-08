package com.czintercity.icsec_app.assessment.dto;

import com.czintercity.icsec_app.assessment.entity.ControlStatus;

import java.util.UUID;

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

    public ControlStatusDTO(UUID assessmentId, UUID controlId){
        this.assessmentId = assessmentId;
        this.controlId = controlId;
    }

    public ControlStatusDTO(ControlStatus status) {
        this.assessmentId = status.getAssessment().getId();
        this.controlId = status.getControl().getId();
        this.coverageMaturity = status.getCoverageMaturity();
        this.coverageScope = status.getCoverageScope();
    }


}