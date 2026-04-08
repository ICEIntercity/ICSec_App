package com.czintercity.icsec_app.relationships.controlRelationship;

import com.czintercity.icsec_app.controls.Control;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.aspectj.asm.internal.Relationship;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="relationship_type")
public abstract class ControlRelationship {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long id;

    @ManyToOne
    @NotNull
    @JoinColumn(name="source_control", referencedColumnName = "id", nullable = false)
    protected Control source;

    @ManyToOne
    @NotNull
    @JoinColumn(name="target_control", referencedColumnName = "id", nullable = false)
    protected Control target;


    public ControlRelationship(Control source, Control target) {
        this.source = source;
        this.target = target;
    }

    public abstract String getDisplayName();
    public abstract Relationship createInverse();

    public Long getId() { return this.id; }
    public Control getSource() { return this.source; }
    public Control getTarget() { return this.target; }
}
