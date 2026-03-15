package com.czintercity.icsec_app.attack;

import jakarta.persistence.Entity;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;

@Entity
public class Tactic {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String mitreId;
    private String name;
    private String description;
    private String mitreLink;

    // GETTERS
    public long getId() { return id; }
    public String getMitreId() {return mitreId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getMitreLink() { return mitreLink; }

    // SETTERS
    public void setMitreId(String mitreId) { this.mitreId = mitreId; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setMitreLink(String mitreLink) { this.mitreLink = mitreLink; }
}
