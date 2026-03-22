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

    public DefaultTechniqueCoverage() {}
}
