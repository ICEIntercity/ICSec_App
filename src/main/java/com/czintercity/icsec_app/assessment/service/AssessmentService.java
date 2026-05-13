package com.czintercity.icsec_app.assessment.service;

import com.czintercity.icsec_app.assessment.dto.AssessmentDTO;
import com.czintercity.icsec_app.assessment.dto.ControlStatusDTO;
import com.czintercity.icsec_app.assessment.dto.MitreCoverageDTO;
import com.czintercity.icsec_app.assessment.dto.TechniquePriorityDTO;
import com.czintercity.icsec_app.assessment.entity.Assessment;
import com.czintercity.icsec_app.assessment.entity.ControlStatus;
import com.czintercity.icsec_app.assessment.repository.AssessmentRepository;
import com.czintercity.icsec_app.assessment.repository.ControlStatusRepository;
import com.czintercity.icsec_app.attack.entity.Tactic;
import com.czintercity.icsec_app.attack.entity.Technique;
import com.czintercity.icsec_app.attack.repository.TacticRepository;
import com.czintercity.icsec_app.attack.repository.TechniqueRepository;
import com.czintercity.icsec_app.controls.entity.Control;
import com.czintercity.icsec_app.controls.repository.ControlRepository;
import com.czintercity.icsec_app.relationships.techniqueCoverage.entity.TechniqueCoverage;
import com.czintercity.icsec_app.topics.entity.Topic;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

/**
 * Core service for creating, loading, and persisting {@link Assessment} records.
 * <p>
 * Responsibilities include building the control-status display map used by the
 * assessment edit view, delegating to repositories for persistence, and managing
 * MITRE ATT&amp;CK technique priorities.
 */
@Service
public class AssessmentService {
    private final ControlRepository controlRepository;
    private final ControlStatusRepository controlStatusRepository;
    private final AssessmentRepository assessmentRepository;
    private final TacticRepository tacticRepository;
    private final TechniqueRepository techniqueRepository;

    public AssessmentService(ControlRepository controlRepository, ControlStatusRepository controlStatusRepository,
                             AssessmentRepository assessmentRepository, TacticRepository tacticRepository,
                             TechniqueRepository techniqueRepository) {
        this.controlRepository = controlRepository;
        this.controlStatusRepository = controlStatusRepository;
        this.assessmentRepository = assessmentRepository;
        this.tacticRepository = tacticRepository;
        this.techniqueRepository = techniqueRepository;
    }

    /**
     * Builds a mapping between all controls and their status in a given {@link AssessmentDTO}
     * @param dto an {@link AssessmentDTO} containing the sparse map of ratings
     * @return a Map containing all available controls and the status of each
     */
    public Map<Control, ControlStatusDTO> buildControlStatusMap(AssessmentDTO dto) {
        Map<UUID, ControlStatusDTO> lookup = new HashMap<>();
        if (dto.getControlStatusMapping() != null) {
            for (ControlStatusDTO statusDTO : dto.getControlStatusMapping()) {
                lookup.put(statusDTO.getControlId(), statusDTO);
            }
        }

        Map<Control, ControlStatusDTO> out = new HashMap<>();
        for (Control control : controlRepository.findAll()) {
            ControlStatusDTO statusDTO = lookup.get(control.getId());
            if (statusDTO != null) {
                out.put(control, statusDTO);
            } else {
                out.put(control, new ControlStatusDTO(dto.getId(), control.getId()));
            }
        }

        return out;
    }

    /**
     * Builds a two-level map of all controls and their statuses, grouped by {@link Topic}.
     * <p>
     * Intended for use in view rendering where controls must be displayed under their parent topic.
     * Controls with no existing assessment entry receive a blank {@link ControlStatus} with zero scores.
     * </p>
     *
     * @param dto the {@link AssessmentDTO} whose saved statuses should be included
     * @return a {@link LinkedHashMap} of topics to their controls and statuses; insertion order is preserved
     * @see #buildControlStatusMap(AssessmentDTO)
     */
    public Map<Topic, Map<Control, ControlStatusDTO>> buildDisplayMap(AssessmentDTO dto) {
        Map<Topic, Map<Control, ControlStatusDTO>> grouped = new LinkedHashMap<>();

        for (Map.Entry<Control, ControlStatusDTO> entry : buildControlStatusMap(dto).entrySet()) {
            Topic topic = entry.getKey().getTopic();

            // Create a bucket for the topic on first encounter
            if (!grouped.containsKey(topic)) {
                grouped.put(topic, new LinkedHashMap<>());
            }

            grouped.get(topic).put(entry.getKey(), entry.getValue());
        }

        return grouped;
    }

    /**
     * Persists an assessment from the given {@link AssessmentDTO}.
     * If an assessment with the same ID already exists it is updated in place;
     * otherwise a new record is created. All existing {@link ControlStatus} entries
     * are replaced with the ones provided in the DTO, skipping any blank entries.
     *
     * @return the saved {@link Assessment} entity
     */
    @Transactional
    public Assessment saveAssessment(AssessmentDTO dto) {
        Optional<Assessment> existing = assessmentRepository.findById(dto.getId());
        Assessment assessment;
        if (existing.isPresent()) {
            assessment = existing.get();
        } else {
            assessment = new Assessment(dto.getId());
        }

        assessment.setName(dto.getName());
        assessment.setDescription(dto.getDescription());

        if(assessment.getControlStatusMapping() != null) {
            controlStatusRepository.deleteAll(assessment.getControlStatusMapping());
        }

        List<ControlStatus> statusList = new ArrayList<>();
        if (dto.getControlStatusMapping() != null) {
            for (ControlStatusDTO statusDTO : dto.getControlStatusMapping()) {
                if (!statusDTO.isBlank()) {
                    statusList.add(new ControlStatus(assessment,
                            controlRepository.getReferenceById(statusDTO.getControlId()),
                            statusDTO.getCoverageMaturity(),
                            statusDTO.getCoverageScope()
                    ));
                }
            }
        }
        assessment.setControlStatusMapping(statusList);
        return assessmentRepository.save(assessment);
    }

    /**
     * Returns all tactics sorted by MITRE ID, each paired with its techniques sorted
     * by MITRE ID. Used to populate the prioritisation and coverage heatmap views.
     */
    @Transactional
    public LinkedHashMap<Tactic, List<Technique>> getTacticsWithTechniques() {
        List<Tactic> tactics = tacticRepository.findAll(Sort.by("mitreId"));
        LinkedHashMap<Tactic, List<Technique>> result = new LinkedHashMap<>();
        for (Tactic tactic : tactics) {
            List<Technique> sorted = new ArrayList<>(tactic.getTechniques());
            sorted.sort(Comparator.comparing(Technique::getMitreId));
            result.put(tactic, sorted);
        }
        return result;
    }

    /**
     * Returns a map of technique ID to priority score for the given assessment,
     * containing only techniques that have been explicitly prioritised (score &gt; 0).
     */
    @Transactional
    public Map<UUID, Short> getTechniquePriorities(UUID assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assessment not found"));
        Map<UUID, Short> priorities = new HashMap<>();
        if (assessment.getTechniquePriorities() != null) {
            for (Map.Entry<Technique, Short> entry : assessment.getTechniquePriorities().entrySet()) {
                priorities.put(entry.getKey().getId(), entry.getValue());
            }
        }
        return priorities;
    }

    /**
     * Saves technique priorities from a DTO for an existing {@link Assessment}.
     *
     * @param assessmentId the {@link UUID} of the {@link Assessment} object that the technique priorities belong to
     * @param priorities a {@link List} of {@link TechniquePriorityDTO} for each technique
     * @return the updated {@link Assessment} object
     */
    @Transactional
    public Assessment saveTechniquePriorities(UUID assessmentId, List<TechniquePriorityDTO> priorities) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assessment not found"));
        Map<Technique, Short> priorityMap = new HashMap<>();
        if (priorities != null) {
            for (TechniquePriorityDTO dto : priorities) {
                if (dto.getPriority() != null && dto.getPriority() > 0) {
                    priorityMap.put(techniqueRepository.getReferenceById(dto.getTechniqueId()), dto.getPriority());
                }
            }
        }
        assessment.setTechniquePriorities(priorityMap);
        return assessmentRepository.save(assessment);
    }
}
