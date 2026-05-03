package com.czintercity.icsec_app.controls.entity;

import com.czintercity.icsec_app.relationships.controlRelationship.entity.ControlRelationship;
import com.czintercity.icsec_app.relationships.techniqueCoverage.entity.TechniqueCoverage;
import com.czintercity.icsec_app.topics.entity.Topic;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class Control {
    @Id
    @Column(updatable = false, nullable = false)
    private UUID id = UUID.randomUUID();

    @Generated(event = EventType.INSERT)
    @Column(
            insertable = false,
            updatable = false,
            columnDefinition = "bigint auto_increment"
    )
    private Long displayId;

    @Column(nullable = false)
    @NotBlank
    private String name;

    @Column(length = 4096)
    private String description;

    @ManyToOne
    @JoinColumn(name="topic_id", referencedColumnName = "id", nullable = false)
    @NotNull
    private Topic topic;

    @Column
    @Min(0)
    @Max(5)
    private Long costIndex;

    @ElementCollection
    @CollectionTable(name="control_references", joinColumns = @JoinColumn(name="control_id"))
    private List<String> references;

    @OneToMany(mappedBy = "control")
    private List<TechniqueCoverage> techniqueCoverage;

    @OneToMany(mappedBy = "source", cascade=CascadeType.REMOVE, orphanRemoval = true)
    private List<ControlRelationship> outgoingRelationships;

    @OneToMany(mappedBy = "target", cascade=CascadeType.REMOVE, orphanRemoval = true)
    private List<ControlRelationship> incomingRelationships;

    // Getters
    public UUID getId() { return this.id; }
    public Long getDisplayId() { return this.displayId; }
    public String getName() { return this.name; }

    /**
     * Derived property getter for the technique code. Generated as a concat of topic code + control ID
     * @return Generated code, or NONE if code can't be generated (Topic or ID is missing)
     */
    public String getCode() {
        if (this.topic != null && this.displayId != null) {
            return this.topic.getCode() + "-" + this.displayId.toString();
        }
            return "NONE";
    }

    public String getDescription() { return this.description; }
    public Topic getTopic() { return this.topic; }
    public Long getCostIndex() { return this.costIndex; }
    public List<String> getReferences() { return this.references; }
    public List<TechniqueCoverage> getTechniqueCoverage() {
        return this.techniqueCoverage != null ? new ArrayList<>(this.techniqueCoverage) : new ArrayList<>();
    }
    public List<ControlRelationship> getOutgoingRelationships() { return this.outgoingRelationships; }
    public List<ControlRelationship> getIncomingRelationships() { return this.incomingRelationships; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setTopic(Topic topic) { this.topic = topic; }
    public void setCostIndex(Long costIndex) { this.costIndex = costIndex; }
    public void setReferences(List<String> references) { this.references = references; }
    public void setTechniqueCoverage(List<TechniqueCoverage> coverage) { this.techniqueCoverage = coverage; }
    public void setOutgoingRelationships(List<ControlRelationship> outgoingRelationships) {
        if(this.outgoingRelationships != null)
            this.outgoingRelationships.clear();
        else
            this.outgoingRelationships = new ArrayList<>();

        if(outgoingRelationships != null) {
            this.outgoingRelationships.addAll(outgoingRelationships);
        }
    }

    // Comparison operators
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Control)) return false;
        return this.id.equals(((Control) o).getId());
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
}