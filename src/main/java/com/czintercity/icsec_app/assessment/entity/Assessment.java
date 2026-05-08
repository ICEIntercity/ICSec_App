package com.czintercity.icsec_app.assessment.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.*;

@Entity
public class Assessment {

    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    @NotBlank
    @NotNull
    private String name;

    @Column
    private String description;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime created;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updated;

    @OneToMany(mappedBy="assessment", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ControlStatus> controlStatusMapping;

    public Assessment() {
        this.id = UUID.randomUUID();
    }

    public Assessment(UUID id) {
        this.id = id;
    }

    // GETTERS
    public UUID getId() { return this.id; }
    public String getName() { return this.name; }
    public String getDescription() { return this.description; }
    public LocalDateTime getCreated() { return this.created; }
    public LocalDateTime getUpdated() { return this.updated; }
    public List<ControlStatus> getControlStatusMapping() { return this.controlStatusMapping; }

    // SETTERS
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setControlStatusMapping(List<ControlStatus> controlStatusMapping) {
        this.controlStatusMapping = controlStatusMapping;
    }

}
