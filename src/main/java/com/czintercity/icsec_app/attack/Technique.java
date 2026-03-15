package com.czintercity.icsec_app.attack;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Technique {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    // MITRE ATT&CK Properties
    private String techniqueId;
    private String name;
    private String description;
    private String url;

    @ManyToMany
    @JoinTable(
            name = "tactic_technique",
            joinColumns = @JoinColumn(name="technique_id"),
            inverseJoinColumns = @JoinColumn(name="tactic_id")
    )
    private Set<Tactic> tactics;

    public String toString(){
        return String.format("Technique [id='%d';mitre_id='%s',name='%s';description='%s';]", id, techniqueId, name, description);
    }

    // GETTERS
    public String getName() { return this.name; }
    public String getTechniqueId(){ return this.techniqueId; }
    public String getDescription(){ return this.description; }
    public String getUrl(){ return this.url; }
    public Set<Tactic> getTactics(){ return this.tactics; }

    // SETTERS
    public void setName(String name) { this.name = name; }
    public void setTechniqueId(String techniqueId) { this.techniqueId = techniqueId; }
    public void setDescription(String description) { this.description = description; }
    public void setUrl(String url) { this.url = url; }
    public void setTactics(Set<Tactic> tactics) { this.tactics = tactics; }
}
