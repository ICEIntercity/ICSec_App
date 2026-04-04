package com.czintercity.icsec_app.relationships.techniqueCoverage;

import com.czintercity.icsec_app.attack.Technique;
import com.czintercity.icsec_app.controls.Control;

/**
 * A non-persistent version of technique coverage (for custom coverage settings)
 */
public class CustomTechniqueCoverage implements TechniqueCoverage{

    private Control control;
    private Technique technique;
    private CoverageType coverageType;
    private Short coverageRating;
    private String justification;

    // GETTERS
    public Control getControl() {
        return control;
    }
    public Technique getTechnique() {
        return technique;
    }
    public CoverageType getCoverageType() {
        return coverageType;
    }
    public Short getCoverageRating() {
        return coverageRating;
    }
    public String getJustification() {
        return justification;
    }

    // SETTERS
    public void SetControl(Control control) { this.control = control; }
    protected void SetTechnique(Technique technique) { this.technique = technique; }
    public void SetCoverageType(CoverageType coverageType) { this.coverageType = coverageType; }
    public void SetCoverageRating(Short rating) { this.coverageRating = rating; }
    public void SetJustification(String justification) { this.justification = justification; }

    public boolean isBlank(){
        return this.control == null && this.technique == null;
    }
}

