package com.czintercity.icsec_app.form;

import com.czintercity.icsec_app.controls.entity.Control;
import com.czintercity.icsec_app.relationships.controlRelationship.entity.ControlRelationship;
import com.czintercity.icsec_app.relationships.controlRelationship.dto.ControlRelationshipDTO;
import com.czintercity.icsec_app.relationships.techniqueCoverage.entity.DefaultTechniqueCoverage;
import com.czintercity.icsec_app.topics.entity.Topic;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EditControlForm {

    private UUID controlId;
    private Long displayId;

    @NotBlank(message = "Control name must not be empty.")
    @Length(max=255, message = "Control name must be at most 255 characters long.")
    private String controlName;

    @Length(max=4096, message = "Description must be at most 4096 characters long.")
    private String controlDescription;

    @Min(value = 0, message="Cost index must be no smaller than 0.")
    @Max(value = 5, message="Cost index must be no greater than 5.")
    private Long controlCostIndex;

    @NotNull(message = "Topic must be set.")
    private Topic topic;
    private List<String> references;
    private List<DefaultTechniqueCoverage> defaultTechniqueCoverage;
    private List<ControlRelationshipDTO> outgoingRelationships;

    public EditControlForm() {
        this.controlId = null;
        this.controlName = null;
        this.controlDescription = null;
        this.topic = null;
        this.references = null;
        this.defaultTechniqueCoverage = new ArrayList<>();
        this.outgoingRelationships = new ArrayList<>();
    }
    public EditControlForm(Control control){
        this.controlId = control.getId();
        this.controlName = control.getName();
        this.controlDescription = control.getDescription();
        this.topic = control.getTopic();
        this.references = new ArrayList<>(control.getReferences());
        this.defaultTechniqueCoverage = new ArrayList<>(control.getDefaultTechniqueCoverage());
        this.outgoingRelationships = new ArrayList<>();

        for(ControlRelationship rel : control.getOutgoingRelationships()){
            this.outgoingRelationships.add(new ControlRelationshipDTO(rel));
        }
    }

    /**
     * Calculates a control code to display. Analogous to the same method in Control
     * @return Control code if applicable, or "NONE".
     */
    public String getControlCode() {
        if (this.topic != null && this.displayId != null) {
            return this.topic.getCode() + "-" + this.displayId.toString();
        }
        return "NONE";
    }

    // GETTERS
    public UUID getControlId() {
        return controlId;
    }
    public String getControlName() {
        return controlName;
    }
    public String getControlDescription() {
        return controlDescription;
    }
    public Long getControlCostIndex() {
        return controlCostIndex;
    }
    public Long getDisplayId() { return displayId; }
    public List<DefaultTechniqueCoverage> getDefaultTechniqueCoverage() { return defaultTechniqueCoverage; }
    public Topic getTopic() { return topic;}
    public List<String> getReferences() {return references;}
    public List<ControlRelationshipDTO> getOutgoingRelationships() { return outgoingRelationships; }

    // SETTERS
    public void setControlName(String controlName) { this.controlName = controlName;}
    public void setReferences(List<String> references) { this.references = references; }
    public void setTopic(Topic topic) { this.topic = topic; }
    public void setDefaultTechniqueCoverage(List<DefaultTechniqueCoverage> defaultTechniqueCoverage) { this.defaultTechniqueCoverage = defaultTechniqueCoverage; }
    public void setControlCostIndex(Long costIndex) { this.controlCostIndex = costIndex; }
    public void setControlId(UUID controlId) { this.controlId = controlId; }
    public void setControlDescription(String controlDescription) { this.controlDescription = controlDescription; }
    public void setDisplayId(Long displayId) { this.displayId = displayId; }
    public void setOutgoingRelationships(List<ControlRelationshipDTO> relationships) { this.outgoingRelationships = relationships; }
}
