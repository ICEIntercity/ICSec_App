package com.czintercity.icsec_app.controls;

import com.czintercity.icsec_app.topics.Topic;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

@Entity
public class Control {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message="Name must not be empty.")
    private String name;

    @Column
    @NotBlank(message="Description must not be empty.")
    private String description;

    @ManyToOne
    @JoinColumn(name="topic_id", referencedColumnName = "id", nullable = false)
    @NotNull(message="Topic must be selected.")
    private Topic topic;

    @ElementCollection
    @CollectionTable(name="control_references", joinColumns = @JoinColumn(name="control_id"))
    private Set<String> references;

    

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
    public Set<String> getReferences() { return this.references; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setTopic(Topic topic) { this.topic = topic; }
}

