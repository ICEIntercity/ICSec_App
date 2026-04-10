package com.czintercity.icsec_app.relationships.controlRelationship;

public enum ControlRelationshipType {
    DEPENDENCY("Dependency"),
    SYNERGY("Synergy"),
    UNKNOWN("Unknown");

    private final String displayValue;
    ControlRelationshipType(String displayValue) {
        this.displayValue = displayValue;
    }
    public String getDisplayValue() {
        return displayValue;
    }
}
