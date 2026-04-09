package com.czintercity.icsec_app.relationships.controlRelationship;

import com.czintercity.icsec_app.controls.Control;
import com.czintercity.icsec_app.controls.ControlRepository;
import com.czintercity.icsec_app.relationships.controlRelationship.dto.ControlRelationshipDTO;
import com.czintercity.icsec_app.relationships.controlRelationship.repository.ControlRelationshipRepository;
import com.czintercity.icsec_app.relationships.controlRelationship.repository.DependencyRepository;
import com.czintercity.icsec_app.relationships.controlRelationship.repository.SynergyRepository;
import com.czintercity.icsec_app.runtime.exception.DuplicateRelationshipException;
import com.czintercity.icsec_app.runtime.exception.RecordNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ControlRelationshipService {

    private final ControlRepository controlRepository;
    private final ControlRelationshipRepository controlRelationshipRepository;
    private final DependencyRepository dependencyRepository;
    private final SynergyRepository synergyRepository;

    public ControlRelationshipService(ControlRepository controlRepository, ControlRelationshipRepository controlRelationshipRepository, DependencyRepository dependencyRepository, SynergyRepository synergyRepository) {
        this.controlRepository = controlRepository;
        this.controlRelationshipRepository = controlRelationshipRepository;
        this.dependencyRepository = dependencyRepository;
        this.synergyRepository = synergyRepository;
    }

    @Deprecated
    public ControlRelationship createNew(UUID sourceID, UUID targetId, ControlRelationshipType controlRelationshipType) {
        ControlRelationship out;

        // Shut up IntelliJ, I know there are
        switch (controlRelationshipType) {
            case DEPENDENCY:
                out = new Dependency();
                break;
            case SYNERGY:
                out = new Synergy();
                break;
            default:
                throw new IllegalArgumentException("Unknown control relationship type");
        }
        Optional<Control> source = controlRepository.findById(sourceID);
        Optional<Control> target = controlRepository.findById(targetId);
        if (target.isPresent()) {
            out.setTarget(target.get());
        }
        if(source.isPresent()) {
            out.setSource(source.get());
        }
        return out;
    }

    @Transactional
    public List<ControlRelationship> createOutgoingRelationships(Iterable<ControlRelationshipDTO> relationships, UUID sourceId) {
        ArrayList<ControlRelationship> outgoingRelationships = new ArrayList<>();
        // Nuke previous outgoing relationships
        if (relationships == null) {
            throw new IllegalArgumentException("Relationships cannot be null");
        }

        for (ControlRelationshipDTO relationship : relationships) {
            // Skip unknowns
            if(relationship.getType() == ControlRelationshipType.UNKNOWN) {
                continue;
            }
            // Validate relationship
            if(relationship.getTargetId() == null) {
                throw new IllegalArgumentException("Target id cannot be null.");
            }

            if(relationship.getSourceId() == null && sourceId == null) {
                throw new IllegalArgumentException("Source id must not be null unless passed to service.");
            }

            ControlRelationship toSave; // Prepare object to save

            // Initialize source and target. Source ID may be provided, if it isn't, take it from the DTO
            Optional<Control> source = (sourceId == null ? controlRepository.findById(relationship.getSourceId()) : controlRepository.findById(sourceId));
            Optional<Control> target = controlRepository.findById(relationship.getTargetId());

            if(source.isEmpty() || target.isEmpty()) {
                throw new RecordNotFoundException("Failed to find source or target record.");
            }

            switch (relationship.getType()) {
                case DEPENDENCY:
                    if(dependencyRepository.existsBySource_IdAndTarget_Id(relationship.getSourceId(), relationship.getTargetId())) {
                        throw new DuplicateRelationshipException("Failed to save relationship: duplicate dependency found.");
                    }

                    toSave = new Dependency();
                    break;

                case SYNERGY:
                    if(synergyRepository.existsBySource_IdAndTarget_Id(relationship.getSourceId(), relationship.getTargetId())) {
                        throw new DuplicateRelationshipException("Failed to save relationship: duplicate synergy found.");
                    }
                    toSave = new Synergy();
                    break;
                case UNKNOWN:
                    continue; // Skip blanks

                default:
                    throw new IllegalArgumentException("Unknown control relationship type.");
            }

            toSave.setSource(source.get());
            toSave.setTarget(target.get());

            toSave = controlRelationshipRepository.save(toSave);
            outgoingRelationships.add(toSave);
        }

        return outgoingRelationships;
    }
}
