package com.czintercity.icsec_app.attack.entity;

import jakarta.persistence.*;

import java.util.Set;
import java.util.UUID;

@Entity
public class Technique {
    @Id
    private UUID id = UUID.randomUUID();

    // MITRE ATT&CK Properties
    @Column(unique=true, nullable = false)
    private String mitreId;

    @Column(nullable = false)
    private String name;

    @Column(length = 8092)
    private String description;

    @Column
    private String mitreLink;

    @ManyToMany
    @JoinTable(
            name = "tactic_technique",
            joinColumns = @JoinColumn(name="technique_id"),
            inverseJoinColumns = @JoinColumn(name="tactic_id")
    )
    private Set<Tactic> tactics;

    public String toString(){
        return String.format("Technique [id='%s';mitre_id='%s',name='%s';description='%s';]", id, mitreId, name, description);
    }

    // GETTERS
    public UUID getId() { return this.id; }
    public String getName() { return this.name; }
    public String getMitreId(){ return this.mitreId; }
    public String getDescription(){ return this.description; }
    public String getMitreLink(){ return this.mitreLink; }
    public Set<Tactic> getTactics(){ return this.tactics; }

    // SETTERS
    public void setId(UUID id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setMitreId(String techniqueId) { this.mitreId = techniqueId; }
    public void setDescription(String description) { this.description = description; }
    public void setMitreLink(String url) { this.mitreLink = url; }
    public void setTactics(Set<Tactic> tactics) { this.tactics = tactics; }

    public String getDisplayLabel() {
        return this.mitreId + " - " + this.name;
    }
}
