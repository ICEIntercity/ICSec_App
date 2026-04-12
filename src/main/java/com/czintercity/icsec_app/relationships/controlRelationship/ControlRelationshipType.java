package com.czintercity.icsec_app.relationships.controlRelationship;

public enum ControlRelationshipType {
    DEPENDENCY("Dependency", "#6610f2"),   // Indigo
    SYNERGY("Synergy", "#20c997"),         // Teal
    ENFORCEMENT("Enforcement", "#d63384"), // Pink/Rose
    SUPPORT("Support", "#198754"),         // Success Green
    COMPENSATION("Compensation", "#fd7e14"),// Orange
    VERIFICATION("Verification", "#ffc107"),// Warning Yellow
    CONFLICT("Conflict", "#dc3545"),       // Danger Red
    UNKNOWN("Unknown", "#6c757d");         // Secondary Gray

    private final String displayValue;
    private final String color;

    ControlRelationshipType(String displayValue, String color) {
        this.displayValue = displayValue;
        this.color = color;
    }

    public String getDisplayValue() { return displayValue; }
    public String getColor() { return color; }
}