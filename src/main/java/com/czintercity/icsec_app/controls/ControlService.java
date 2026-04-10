package com.czintercity.icsec_app.controls;

import com.czintercity.icsec_app.form.EditControlForm;
import com.czintercity.icsec_app.relationships.controlRelationship.ControlRelationshipService;
import com.czintercity.icsec_app.relationships.controlRelationship.repository.ControlRelationshipRepository;
import com.czintercity.icsec_app.relationships.techniqueCoverage.DefaultTechniqueCoverage;
import com.czintercity.icsec_app.relationships.techniqueCoverage.DefaultTechniqueCoverageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ControlService {
    private static final Logger logger = LoggerFactory.getLogger(ControlService.class);
    private final ControlRepository controlRepository;
    private final DefaultTechniqueCoverageRepository techniqueCoverageRepository;
    private final ControlRelationshipService controlRelationshipService;
    private final ControlRelationshipRepository controlRelationshipRepository;
    private final DefaultTechniqueCoverageRepository defaultTechniqueCoverageRepository;

    public ControlService(ControlRepository controlRepository, DefaultTechniqueCoverageRepository techniqueCoverageRepository, ControlRelationshipService controlRelationshipService, ControlRelationshipRepository controlRelationshipRepository, DefaultTechniqueCoverageRepository defaultTechniqueCoverageRepository) {
        this.controlRepository = controlRepository;
        this.techniqueCoverageRepository = techniqueCoverageRepository;
        this.controlRelationshipService = controlRelationshipService;
        this.controlRelationshipRepository = controlRelationshipRepository;
        this.defaultTechniqueCoverageRepository = defaultTechniqueCoverageRepository;
    }

    @Transactional
    public Control createOrUpdateFromForm(EditControlForm form){
        Control control;

        // Check if creating or updating
        if(form.getControlId() != null){
            Optional<Control> existing = controlRepository.findById(form.getControlId());
            if(existing.isPresent()){
                control = existing.get();

                // Clear existing MITRE mapping
                if(control.getDefaultTechniqueCoverage() != null){
                    defaultTechniqueCoverageRepository.deleteAll(control.getDefaultTechniqueCoverage());
                    control.setDefaultTechniqueCoverage(null);
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
        List<DefaultTechniqueCoverage> techniqueCoverage = new ArrayList<>();
        for(DefaultTechniqueCoverage coverage : form.getDefaultTechniqueCoverage()){
            if(coverage.isBlank()) continue; // Skip null entries

            coverage.setControl(control);
            coverage = techniqueCoverageRepository.save(coverage);
            techniqueCoverage.add(coverage);
        }
        control.setDefaultTechniqueCoverage(techniqueCoverage);

        control.setOutgoingRelationships(
                controlRelationshipService.createOutgoingRelationships(form.getOutgoingRelationships(), control.getId())
        );

        return controlRepository.save(control);
    }
}
