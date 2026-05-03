package com.czintercity.icsec_app.relationships.techniqueCoverage.entity;

import com.czintercity.icsec_app.attack.entity.Technique;
import com.czintercity.icsec_app.controls.entity.Control;
import com.czintercity.icsec_app.relationships.techniqueCoverage.CoverageType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;

/**
 * Persistent entity representing a security control's coverage of a MITRE ATT&CK for ICS technique.
 *
 * <p>Each instance associates a {@link Control} with a {@link Technique} and describes how
 * effectively the control addresses that technique via a numeric {@code coverageRating} (0–5),
 * a {@link CoverageType} category, and an optional free-text {@code justification}.
 *
 * <p>Rows with both {@code control} and {@code technique} set to {@code null} are considered
 * blank placeholders (see {@link #isBlank()}) and are skipped during persistence.
 */
@Entity
@Table(name = "technique_coverage")
public class TechniqueCoverage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "control_id", referencedColumnName = "id", nullable = false)
    private Control control;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "technique_id", referencedColumnName = "id", nullable = false)
    private Technique technique;

    @Enumerated(EnumType.STRING)
    CoverageType coverageType;

    @Column(nullable = false)
    @NotNull
    @Max(5)
    private Short coverageRating;

    @Column(length = 8192)
    private String justification;

    // GETTERS
    public Long getId() { return id; }
    public Control getControl() { return control; }
    public Technique getTechnique() { return technique; }
    public CoverageType getCoverageType() { return coverageType; }
    public Short getCoverageRating() { return coverageRating; }
    public String getJustification() { return justification; }

    // SETTERS
    public void setTechnique(Technique technique) { this.technique = technique; }
    public void setControl(Control control) { this.control = control; }
    public void setCoverageType(CoverageType coverageType) { this.coverageType = coverageType; }
    public void setCoverageRating(Short rating) { this.coverageRating = rating; }
    public void setJustification(String justification) { this.justification = justification; }

    public TechniqueCoverage() {}

    /** Returns {@code true} if both {@code control} and {@code technique} are unset. */
    public boolean isBlank() {
        return this.control == null && this.technique == null;
    }
}