package com.czintercity.icsec_app.relationships.techniqueCoverage;

import com.czintercity.icsec_app.attack.Technique;
import com.czintercity.icsec_app.controls.Control;

public interface TechniqueCoverage {
    Control getControl();
    Technique getTechnique();
    CoverageType getCoverageType();
    Short getCoverageRating();
    String getJustification();
}
