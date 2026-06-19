package com.czintercity.icsec_app.relationships.techniqueCoverage;

/**
 * Classifies the mechanism by which a security control addresses a MITRE ATT&amp;CK for ICS technique.
 *
 * <ul>
 *   <li>{@link #DETERRENT} — discourages an attacker from attempting the technique.</li>
 *   <li>{@link #PREVENTATIVE} — blocks or stops the technique from succeeding.</li>
 *   <li>{@link #DETECTIVE} — identifies or alerts on the use of the technique.</li>
 *   <li>{@link #RECOVERY} — restores normal operation after the technique has been executed.</li>
 *   <li>{@link #CONTAINMENT} — limits the blast radius or lateral spread of the technique.</li>
 *   <li>{@link #UNKNOWN} — fallback used when the coverage type cannot be determined.</li>
 * </ul>
 */
public enum CoverageType {
    /** Discourages an attacker from attempting the technique. */
    DETERRENT("Deterrent", "#F59E0B"),
    /** Blocks or stops the technique from succeeding. */
    PREVENTATIVE("Preventative", "#EF4444"),
    /** Identifies or alerts on the use of the technique. */
    DETECTIVE("Detective", "#3B82F6"),
    /** Restores normal operation after the technique has been executed. */
    RECOVERY("Recovery", "#10B981"),
    /** Limits the blast radius or lateral spread of the technique. */
    CONTAINMENT("Containment", "#8B5CF6"),
    /** Fallback used when the coverage type cannot be determined. */
    UNKNOWN("Unknown", "#6B7280");

    private final String displayValue;
    private final String hexColor;

    CoverageType(String displayValue, String hexColor) {
        this.displayValue = displayValue;
        this.hexColor = hexColor;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public String getHexColor() {
        return hexColor;
    }

    /**
     * Computes a CSS {@code rgb()} colour that is a linear tint of this type's base colour, used to
     * shade the coverage heatmap cards. A score of 0 produces white; a score of 5 produces the
     * unmodified base colour.
     *
     * @param score coverage score in the range [0, 5]
     * @return CSS colour string in the form {@code rgb(r,g,b)}
     */
    public String tintColor(double score) {
        int r = Integer.parseInt(hexColor.substring(1, 3), 16);
        int g = Integer.parseInt(hexColor.substring(3, 5), 16);
        int b = Integer.parseInt(hexColor.substring(5, 7), 16);

        double t = score / 5.0;
        if (t < 0.0) t = 0.0;
        if (t > 1.0) t = 1.0;

        int tr = (int) Math.round(255 + (r - 255) * t);
        int tg = (int) Math.round(255 + (g - 255) * t);
        int tb = (int) Math.round(255 + (b - 255) * t);

        return String.format("rgb(%d,%d,%d)", tr, tg, tb);
    }
}
