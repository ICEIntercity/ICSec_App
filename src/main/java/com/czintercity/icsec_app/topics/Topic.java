package com.czintercity.icsec_app.topics;

import com.czintercity.icsec_app.controls.Control;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Topic {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true, nullable = false)
    @NotBlank(message="Code must not be empty.")
    @Length(min=3, max=3, message="Code must be 3 characters long")
    private String code;

    @Column(unique = true, nullable = false)
    @NotBlank(message="Name cannot be empty.")
    @Length(min=3, max=40, message="Name must be between 3 and 40 characters long.")
    private String name;

    @Column
    @NotBlank(message = "Description cannot be empty.")
    @Length(max=4096, message="Description must be 4096 characters or shorter.")
    private String description;

    @OneToMany(mappedBy="topic")
    Set<Control> controls;

    /**
     * Generates a topic code from its name by taking the first three letters and capitalizing them. Does not guarantee
     * a unique code, and is only intended to generate placeholders.
     *
     * @return Placeholder code.
     */
    public String generatePlaceholderCode(){
        return this.name.substring(0, 2).toUpperCase();
    }

    // GETTERS
    public Long getId() { return this.id; }
    public String getCode() { return this.code; }
    public String getName() { return this.name; }
    public String getDescription() { return this.description; }
    public Iterable<Control> getControls() { return this.controls;}

    public void setName(String name) {
        this.name = name;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setControls(Set<Control> controls) {
        this.controls = controls;
    }

    protected Topic(){
        this.name = "";
        this.description = "";
        this.code = "";
    }

    public Topic(String code, String name, String description) {
        this.name = name;
        this.description = description;
        this.code = code;
        this.controls = new HashSet<Control>();
    }

    public String toString() {
        return this.code;
    }
}
