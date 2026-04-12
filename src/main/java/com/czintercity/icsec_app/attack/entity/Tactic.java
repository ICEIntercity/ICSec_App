package com.czintercity.icsec_app.attack.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

@Entity
public class Tactic {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(unique = true)
    private String mitreId;

    @Column(unique = true, nullable = false)
    @NotEmpty
    private String name;

    @Column(length = 8092)
    private String description;

    @Column
    private String mitreLink;

    @ManyToMany(mappedBy = "tactics")
    private List<Technique> techniques;

    // GETTERS
    public UUID getId() { return id; }
    public String getMitreId() {return mitreId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getMitreLink() { return mitreLink; }
    public List<Technique> getTechniques() { return techniques; }

    // SETTERS
    public void setId(UUID id) { this.id = id; }
    public void setMitreId(String mitreId) { this.mitreId = mitreId; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setMitreLink(String mitreLink) { this.mitreLink = mitreLink; }
}
