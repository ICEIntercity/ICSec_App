package com.czintercity.icsec_app.relationships.techniqueCoverage;

import com.czintercity.icsec_app.attack.Technique;
import com.czintercity.icsec_app.controls.Control;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
public class DefaultTechniqueCoverage implements TechniqueCoverage{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "control_id", referencedColumnName = "id", nullable = false)
    private Control control;

    @ManyToOne
    @JoinColumn(name = "technique_id", referencedColumnName = "id", nullable = false)
    private Technique technique;

    @Enumerated(EnumType.STRING)
    CoverageType coverageType;

    @Column(nullable = false)
    @NotNull
    @Min(1)
    @Max(5)
    private Short coverageRating;

    @Column(nullable = true, length = 8192)
    private String justification;

    // GETTERS
    public Long getId() {
        return id;
    }

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
    public void SetCoverageRating(Short rating) { this.coverageRating = rating; }
    public void SetJustification(String justification) { this.justification = justification; }

    // Protected blank constructor (Needed by JPA)
    protected DefaultTechniqueCoverage() {}

    // Full constructor
    public DefaultTechniqueCoverage(Control control, Technique technique, Short coverageRating, String justification) {
        this.control = control;
        this.technique = technique;
        this.coverageRating = coverageRating;
        this.justification = justification;
    }

    // Partial constructor (for when coverage rating is not set)
    public DefaultTechniqueCoverage(Control control, Technique technique){
        this.control = control;
        this.technique = technique;
        this.coverageRating = 0;
        this.justification = "No coverage info provided.";
    }
}

