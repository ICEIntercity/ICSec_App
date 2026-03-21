package com.czintercity.icsec_app.controls;

import com.czintercity.icsec_app.relationships.techniqueCoverage.DefaultTechniqueCoverage;
import com.czintercity.icsec_app.relationships.techniqueCoverage.TechniqueCoverage;
import com.czintercity.icsec_app.topics.Topic;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Control {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 4096)
    private String description;

    @ManyToOne
    @JoinColumn(name="topic_id", referencedColumnName = "id", nullable = false)
    private Topic topic;

    @ElementCollection
    @CollectionTable(name="control_references", joinColumns = @JoinColumn(name="control_id"))
    private List<String> references;

    @OneToMany(mappedBy = "control")
    private List<DefaultTechniqueCoverage> defaultTechniqueCoverage;

    @Transient
    private List<TechniqueCoverage> customTechniqueCoverage;

    // Getters
    public Long getId() { return this.id; }
    public String getName() { return this.name; }

    /**
     * Derived property getter for the technique code. Generated as a concat of topic code + control ID
     * @return Generated code, or NONE if code can't be generated (Topic or ID is missing)
     */
    public String getCode() {
        if (this.topic != null && this.id != null) {
            return this.topic.getCode() + "-" + this.id.toString();
        }
            return "NONE";
    }

    public String getDescription() { return this.description; }
    public Topic getTopic() { return this.topic; }
    public List<String> getReferences() { return this.references; }
    public List<DefaultTechniqueCoverage> getDefaultTechniqueCoverage() {
        return this.defaultTechniqueCoverage;
    }

    /**
     * Get technique coverage.
     * Returns default technique coverage if no override is present, otherwise, it returns custom technique coverage.
     *
     * @return Custom Technique Coverage, or default if custom is not present.
     */
    public List<TechniqueCoverage> getTechniqueCoverage(){
        if(customTechniqueCoverage.isEmpty()){
            return new ArrayList<>(defaultTechniqueCoverage);
        }
        else
            return customTechniqueCoverage;
    }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setTopic(Topic topic) { this.topic = topic; }
    public void setReferences(List<String> references) { this.references = references; }
    public void setDefaultTechniqueCoverage(List<DefaultTechniqueCoverage> coverage) { this.defaultTechniqueCoverage = coverage; }
}

