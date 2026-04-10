package com.czintercity.icsec_app.relationships.controlRelationship;

import com.czintercity.icsec_app.controls.Control;
import com.czintercity.icsec_app.relationships.controlRelationship.record.ControlRelationshipVisuals;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="relationship_type")
public abstract class ControlRelationship {

    @Id
    @Column(unique = true, nullable = false, updatable = false)
    private UUID id = UUID.randomUUID();

    @ManyToOne
    @NotNull
    @JoinColumn(name="source_control", referencedColumnName = "id", nullable = false)
    private Control source;

    @ManyToOne
    @NotNull
    @JoinColumn(name="target_control", referencedColumnName = "id", nullable = false)
    private Control target;

    /**
     * Default constructor for ControlRelationship
     */
    public ControlRelationship() {}

    /**
     * Full constructor for ControlRelationship
     * @param source Source technique for the relationship
     * @param target Target technique for the relationship
     */
    public ControlRelationship(Control source, Control target) {
        this.source = source;
        this.target = target;
    }

    public abstract String getDisplayName();

    public UUID getId() { return this.id; }
    public Control getSource() { return this.source; }
    public Control getTarget() { return this.target; }

    public void setId(UUID id) { this.id = id; }
    public void setSource(Control source) { this.source = source; }
    public void setTarget(Control target) { this.target = target; }

    protected abstract String getInboundName();
    protected abstract String getOutboundName();

    public abstract ControlRelationshipType getType();
    public abstract ControlRelationshipVisuals getVisuals();

    public String getDisplayName(Control context) throws IllegalArgumentException {
        if(context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }

        if (Objects.equals(getSource(), context)) {
            return getOutboundName();
        }

        if (Objects.equals(getTarget(), context)) {
            return getInboundName();
        }

        throw new IllegalArgumentException(
                String.format("Control [%s] is not part of this dependency (%s -> %s)",
                        context.getId(),
                        getSource() != null ? getSource().getId() : "null",
                        getTarget() != null ? getTarget().getId() : "null")
        );
    }

    public Map<String, Object> getGraphProperties(Control context) throws IllegalArgumentException {
        if(context == null) { throw new IllegalArgumentException("Context cannot be null"); }

        HashMap<String, Object> out = new HashMap<>();
        ControlRelationshipVisuals visuals = getVisuals();

        out.put("label", getDisplayName(context));
        out.put("color", visuals.color());
        out.put("dashed", visuals.dashed());
        out.put("arrowType", visuals.arrowType());

        return out;
    }
}
