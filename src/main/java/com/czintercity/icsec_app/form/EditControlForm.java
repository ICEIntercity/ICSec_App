package com.czintercity.icsec_app.form;

import com.czintercity.icsec_app.controls.Control;
import com.czintercity.icsec_app.relationships.techniqueCoverage.DefaultTechniqueCoverage;
import com.czintercity.icsec_app.topics.Topic;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.util.ArrayList;
import java.util.List;

public class EditControlForm {

    private Long controlId;

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

    public EditControlForm() {
        this.controlId = null;
        this.controlName = null;
        this.controlDescription = null;
        this.topic = null;
        this.references = null;
        this.defaultTechniqueCoverage = new ArrayList<>();
    }
    public EditControlForm(Control control){
        this.controlId = control.getId();
        this.controlName = control.getName();
        this.controlDescription = control.getDescription();
        this.topic = control.getTopic();
        this.references = new ArrayList<>(control.getReferences());
        this.defaultTechniqueCoverage = new ArrayList<>(control.getDefaultTechniqueCoverage());
    }

    public String getControlCode() {
        if (this.topic != null && this.controlId != null) {
            return this.topic.getCode() + "-" + this.controlId.toString();
        }
        return "NONE";
    }

    public Long getControlId() {
        return controlId;
    }

    public void setControlId(Long controlId) {
        this.controlId = controlId;
    }

    public String getControlName() {
        return controlName;
    }

    public void setControlName(String controlName) {
        this.controlName = controlName;
    }

    public String getControlDescription() {
        return controlDescription;
    }

    public void setControlDescription(String controlDescription) {
        this.controlDescription = controlDescription;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public List<String> getReferences() {
        return references;
    }

    public void setReferences(List<String> references) {
        this.references = references;
    }

    public List<DefaultTechniqueCoverage> getDefaultTechniqueCoverage() {
        return defaultTechniqueCoverage;
    }

    public void setDefaultTechniqueCoverage(List<DefaultTechniqueCoverage> defaultTechniqueCoverage) {
        this.defaultTechniqueCoverage = defaultTechniqueCoverage;
    }

    public Long getControlCostIndex() {
        return controlCostIndex;
    }

    public void setControlCostIndex(Long costIndex) {
        this.controlCostIndex = costIndex;
    }
}
