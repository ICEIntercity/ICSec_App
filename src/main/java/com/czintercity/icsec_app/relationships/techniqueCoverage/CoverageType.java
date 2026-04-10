package com.czintercity.icsec_app.relationships.techniqueCoverage;

public enum CoverageType {
    DETERRENT("Deterrent"),
    PREVENTATIVE("Preventative"),
    DETECTIVE("Detective"),
    RECOVERY("Recovery"),
    CONTAINMENT("Containment"),
    UNKNOWN("Unknown");

    private final String displayValue;
    private CoverageType(String displayValue) {
        this.displayValue = displayValue;
    }
    public String getDisplayValue() {
        return displayValue;
    }
}
