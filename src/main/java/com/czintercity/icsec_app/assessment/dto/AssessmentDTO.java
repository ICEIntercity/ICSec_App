package com.czintercity.icsec_app.assessment.dto;

import com.czintercity.icsec_app.assessment.entity.Assessment;
import com.czintercity.icsec_app.assessment.entity.ControlStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data transfer object representing an {@link com.czintercity.icsec_app.assessment.entity.Assessment}.
 * Used for form binding on the assessment edit view and as a lightweight projection
 * passed between the controller and service layers.
 * <p>
 * A fresh instance is always assigned a random {@link java.util.UUID} so that a new
 * assessment can be persisted without a separate ID-generation step.
 */
public class AssessmentDTO {
    private UUID id;
    private String name;
    private String description;
    private List<ControlStatusDTO> controlStatusMapping;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<ControlStatusDTO> getControlStatusMapping() { return controlStatusMapping; }
    public void setControlStatusMapping(List<ControlStatusDTO> controlStatusMapping) { this.controlStatusMapping = controlStatusMapping; }

    public AssessmentDTO() {
        this.id = UUID.randomUUID();
    }

    /**
     * Constructs an {@code AssessmentDTO} from an existing {@link Assessment} entity,
     * converting each {@link com.czintercity.icsec_app.assessment.entity.ControlStatus}
     * into a {@link ControlStatusDTO}.
     */
    public AssessmentDTO(Assessment assessment) {
        this.id = assessment.getId();
        this.name = assessment.getName();
        this.description = assessment.getDescription();
        this.controlStatusMapping = new ArrayList<>();
        if (assessment.getControlStatusMapping() != null) {
            for (ControlStatus status : assessment.getControlStatusMapping()) {
                this.controlStatusMapping.add(new ControlStatusDTO(status));
            }
        }
    }
}