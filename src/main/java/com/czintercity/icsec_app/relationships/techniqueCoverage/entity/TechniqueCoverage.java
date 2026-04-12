package com.czintercity.icsec_app.relationships.techniqueCoverage.entity;

import com.czintercity.icsec_app.attack.entity.Technique;
import com.czintercity.icsec_app.controls.entity.Control;
import com.czintercity.icsec_app.relationships.techniqueCoverage.CoverageType;

public interface TechniqueCoverage {
    Control getControl();
    Technique getTechnique();
    CoverageType getCoverageType();
    Short getCoverageRating();
    String getJustification();

    boolean isBlank();
}
