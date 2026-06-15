package com.czintercity.icsec_app.relationships.controlRelationship;

import com.czintercity.icsec_app.controls.entity.Control;
import com.czintercity.icsec_app.controls.repository.ControlRepository;
import com.czintercity.icsec_app.relationships.controlRelationship.dto.ControlRelationshipDTO;
import com.czintercity.icsec_app.relationships.controlRelationship.dto.RelationshipImportResult;
import com.czintercity.icsec_app.relationships.controlRelationship.entity.*;
import com.czintercity.icsec_app.relationships.controlRelationship.repository.*;
import com.czintercity.icsec_app.runtime.exception.DuplicateRelationshipException;
import com.czintercity.icsec_app.runtime.exception.RecordNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service responsible for creating typed {@link ControlRelationship} entities from
 * {@link ControlRelationshipDTO} descriptors submitted in the control edit form.
 * Enforces uniqueness constraints per relationship type and resolves source/target controls
 * from the repository within a single transaction.
 */
@Service
public class ControlRelationshipService {

    private final ControlRepository controlRepository;
    private final ControlRelationshipRepository controlRelationshipRepository;
    private final DependencyRepository dependencyRepository;
    private final SynergyRepository synergyRepository;
    private final SupportRepository supportRepository;
    private final RedundancyRepository redundancyRepository;
    private final EnforcementRepository enforcementRepository;
    private final CompensationRepository compensationRepository;
    private final ConflictRepository conflictRepository;

    public ControlRelationshipService(ControlRepository controlRepository, ControlRelationshipRepository controlRelationshipRepository, DependencyRepository dependencyRepository, SynergyRepository synergyRepository, SupportRepository supportRepository, RedundancyRepository redundancyRepository, EnforcementRepository enforcementRepository, CompensationRepository compensationRepository, ConflictRepository conflictRepository) {
        this.controlRepository = controlRepository;
        this.controlRelationshipRepository = controlRelationshipRepository;
        this.dependencyRepository = dependencyRepository;
        this.synergyRepository = synergyRepository;
        this.supportRepository = supportRepository;
        this.redundancyRepository = redundancyRepository;
        this.enforcementRepository = enforcementRepository;
        this.compensationRepository = compensationRepository;
        this.conflictRepository = conflictRepository;
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

    /**
     * Persists a collection of outgoing relationships originating from the control identified by {@code sourceId}.
     * Relationships of type {@code UNKNOWN} are silently skipped.
     *
     * @param relationships DTOs describing each relationship to create
     * @param sourceId      ID of the source control; overrides {@link ControlRelationshipDTO#getSourceId()} when non-null
     * @return list of persisted {@link ControlRelationship} entities
     * @throws IllegalArgumentException       if {@code relationships} is null, or a target/source ID is missing
     * @throws com.czintercity.icsec_app.runtime.exception.RecordNotFoundException      if the source or target control cannot be found
     * @throws com.czintercity.icsec_app.runtime.exception.DuplicateRelationshipException if an identical relationship already exists
     */
    @Transactional
    public List<ControlRelationship> createOutgoingRelationships(Iterable<ControlRelationshipDTO> relationships, UUID sourceId) {
        ArrayList<ControlRelationship> outgoingRelationships = new ArrayList<>();
        // Nuke previous outgoing relationships
        if (relationships == null) {
            throw new IllegalArgumentException("Relationships cannot be null");
        }

        for (ControlRelationshipDTO relationship : relationships) {
            ControlRelationship saved = buildAndSaveRelationship(relationship, sourceId);
            if (saved != null) {
                outgoingRelationships.add(saved);
            }
        }

        return outgoingRelationships;
    }

    /**
     * Validates a single relationship descriptor, resolves its source/target controls, enforces the
     * per-type uniqueness constraint, and persists it.
     *
     * @param relationship the relationship descriptor to persist
     * @param sourceId     ID of the source control; overrides {@link ControlRelationshipDTO#getSourceId()} when non-null
     * @return the persisted {@link ControlRelationship}, or {@code null} if the descriptor's type is {@code UNKNOWN}
     * @throws IllegalArgumentException                                                   if a target/source ID is missing or the type is unrecognized
     * @throws com.czintercity.icsec_app.runtime.exception.RecordNotFoundException        if the source or target control cannot be found
     * @throws com.czintercity.icsec_app.runtime.exception.DuplicateRelationshipException if an identical relationship already exists
     */
    private ControlRelationship buildAndSaveRelationship(ControlRelationshipDTO relationship, UUID sourceId) {
        // Skip unknowns
        if (relationship.getType() == ControlRelationshipType.UNKNOWN) {
            return null;
        }
        // Validate relationship
        if (relationship.getTargetId() == null) {
            throw new IllegalArgumentException("Target id cannot be null.");
        }

        if (relationship.getSourceId() == null && sourceId == null) {
            throw new IllegalArgumentException("Source id must not be null unless passed to service.");
        }

        ControlRelationship toSave; // Prepare object to save

        // Initialize source and target. Source ID may be provided, if it isn't, take it from the DTO
        Optional<Control> source = (sourceId == null ? controlRepository.findById(relationship.getSourceId()) : controlRepository.findById(sourceId));
        Optional<Control> target = controlRepository.findById(relationship.getTargetId());

        if (source.isEmpty() || target.isEmpty()) {
            throw new RecordNotFoundException("Failed to find source or target record.");
        }

        switch (relationship.getType()) {
            case DEPENDENCY:
                if (dependencyRepository.existsBySource_IdAndTarget_Id(relationship.getSourceId(), relationship.getTargetId())) {
                    throw new DuplicateRelationshipException("Failed to save relationship: duplicate dependency found.");
                }

                toSave = new Dependency();
                break;

            case SYNERGY:
                if (synergyRepository.existsBySource_IdAndTarget_Id(relationship.getSourceId(), relationship.getTargetId())) {
                    throw new DuplicateRelationshipException("Failed to save relationship: duplicate synergy found.");
                }
                toSave = new Synergy();
                break;
            case SUPPORT:
                if (supportRepository.existsBySource_IdAndTarget_Id(relationship.getSourceId(), relationship.getTargetId())) {
                    throw new DuplicateRelationshipException("Failed to save relationship: duplicate support found.");
                }
                toSave = new Support();
                break;
            case REDUNDANCY:
                if (redundancyRepository.existsBySource_IdAndTarget_Id(relationship.getSourceId(), relationship.getTargetId())) {
                    throw new DuplicateRelationshipException("Failed to save relationship: duplicate redundancy found.");
                }
                toSave = new Redundancy();
                break;
            case ENFORCEMENT:
                if (enforcementRepository.existsBySource_IdAndTarget_Id(relationship.getSourceId(), relationship.getTargetId())) {
                    throw new DuplicateRelationshipException("Failed to save relationship: duplicate enforcement found.");
                }
                toSave = new Enforcement();
                break;
            case COMPENSATION:
                if (compensationRepository.existsBySource_IdAndTarget_Id(relationship.getSourceId(), relationship.getTargetId())) {
                    throw new DuplicateRelationshipException("Failed to save relationship: duplicate compensation found.");
                }
                toSave = new Compensation();
                break;
            case CONFLICT:
                if (conflictRepository.existsBySource_IdAndTarget_Id(relationship.getSourceId(), relationship.getTargetId())) {
                    throw new DuplicateRelationshipException("Failed to save relationship: duplicate conflict found.");
                }
                toSave = new Conflict();
                break;

            default:
                throw new IllegalArgumentException("Unknown control relationship type.");
        }

        toSave.setSource(source.get());
        toSave.setTarget(target.get());

        return controlRelationshipRepository.save(toSave);
    }

    /**
     * Bulk-imports a collection of control relationships, each carrying its own source and target control IDs.
     * Good relationships are persisted; any row that fails validation, references a missing control, or
     * duplicates an existing relationship (same source, target and type) is skipped and reported, without
     * affecting the rest of the batch.
     *
     * <p>The whole batch runs in a single transaction. Per-row failures are caught here, before they can
     * cross the transactional boundary, so the transaction is never marked rollback-only; because every
     * validation throws <em>before</em> the relationship is saved, the persistence context stays consistent
     * and all accepted rows are committed.
     *
     * @param relationships DTOs describing each relationship to create
     * @return a {@link RelationshipImportResult} summarising how many were imported and why any were skipped
     * @throws IllegalArgumentException if {@code relationships} is null
     */
    @Transactional
    public RelationshipImportResult importRelationships(Iterable<ControlRelationshipDTO> relationships) {
        if (relationships == null) {
            throw new IllegalArgumentException("Relationships cannot be null");
        }

        int imported = 0;
        List<String> errors = new ArrayList<>();
        int row = 0;

        for (ControlRelationshipDTO relationship : relationships) {
            row++;
            try {
                ControlRelationship saved = buildAndSaveRelationship(relationship, null);
                if (saved != null) {
                    imported++;
                } else {
                    errors.add("Row " + row + ": skipped (UNKNOWN relationship type).");
                }
            } catch (Exception e) {
                errors.add("Row " + row + ": " + e.getMessage());
            }
        }

        return new RelationshipImportResult(imported, errors);
    }
}
