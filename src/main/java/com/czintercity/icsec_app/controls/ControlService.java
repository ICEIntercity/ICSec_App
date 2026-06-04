package com.czintercity.icsec_app.controls;

import com.czintercity.icsec_app.controls.entity.Control;
import com.czintercity.icsec_app.controls.repository.ControlRepository;
import com.czintercity.icsec_app.controls.form.EditControlForm;
import com.czintercity.icsec_app.relationships.controlRelationship.ControlRelationshipService;
import com.czintercity.icsec_app.relationships.controlRelationship.repository.ControlRelationshipRepository;
import com.czintercity.icsec_app.relationships.techniqueCoverage.entity.TechniqueCoverage;
import com.czintercity.icsec_app.relationships.techniqueCoverage.repository.TechniqueCoverageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service responsible for persisting {@link Control} entities from user-submitted form data.
 * Handles creation, update, and replacement of associated technique coverage records
 * and outgoing control relationships within a single transaction.
 */
@Service
public class ControlService {
    private static final Logger logger = LoggerFactory.getLogger(ControlService.class);
    private final ControlRepository controlRepository;
    private final TechniqueCoverageRepository techniqueCoverageRepository;
    private final ControlRelationshipService controlRelationshipService;
    private final ControlRelationshipRepository controlRelationshipRepository;

    public ControlService(ControlRepository controlRepository, TechniqueCoverageRepository techniqueCoverageRepository, ControlRelationshipService controlRelationshipService, ControlRelationshipRepository controlRelationshipRepository) {
        this.controlRepository = controlRepository;
        this.techniqueCoverageRepository = techniqueCoverageRepository;
        this.controlRelationshipService = controlRelationshipService;
        this.controlRelationshipRepository = controlRelationshipRepository;
    }

    /**
     * Creates a new control or updates an existing one based on the provided form data.
     * When updating, existing technique coverage and outgoing relationships are fully replaced.
     *
     * @param form validated form data submitted by the user
     * @return the saved {@link Control} entity
     * @throws IllegalArgumentException if the form references a non-existent control ID
     */
    @Transactional
    public Control createOrUpdateFromForm(EditControlForm form){
        Control control;

        // Check if creating or updating
        if(form.getControlId() != null){
            Optional<Control> existing = controlRepository.findById(form.getControlId());
            if(existing.isPresent()){
                control = existing.get();

                // Clear existing MITRE mapping
                if(control.getTechniqueCoverage() != null){
                    techniqueCoverageRepository.deleteAll(control.getTechniqueCoverage());
                    control.setTechniqueCoverage(null);
                }

                // Clear existing outgoing relationships
                if(control.getOutgoingRelationships() != null){
                    controlRelationshipRepository.deleteAll(control.getOutgoingRelationships());
                    control.setOutgoingRelationships(null);
                }
            }
            else {
                throw new IllegalArgumentException("Control with ID " + form.getControlId() + " not found.");
            }
        }
        else {
            control = new Control();
            form.setControlId(control.getId());
        }

        // Handle simple fields
        control.setName(form.getControlName());
        control.setCostIndex(form.getControlCostIndex());
        control.setDescription(form.getControlDescription());
        control.setTopic(form.getTopic());
        control.setReferences(form.getReferences());

        // Ensure the entity becomes managed before we start giving it bidirectional references
        control = controlRepository.save(control);

        // Handle technique coverage
        List<TechniqueCoverage> techniqueCoverage = new ArrayList<>();
        for(TechniqueCoverage coverage : form.getTechniqueCoverage()){
            if(coverage.isBlank()) continue; // Skip null entries

            coverage.setControl(control);
            coverage = techniqueCoverageRepository.save(coverage);
            techniqueCoverage.add(coverage);
        }
        control.setTechniqueCoverage(techniqueCoverage);

        control.setOutgoingRelationships(
                controlRelationshipService.createOutgoingRelationships(form.getOutgoingRelationships(), control.getId())
        );

        return controlRepository.save(control);
    }
}