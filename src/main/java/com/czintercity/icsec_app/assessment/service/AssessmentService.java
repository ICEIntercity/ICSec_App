package com.czintercity.icsec_app.assessment.service;

import com.czintercity.icsec_app.assessment.dto.AssessmentDTO;
import com.czintercity.icsec_app.assessment.dto.ControlStatusDTO;
import com.czintercity.icsec_app.assessment.entity.Assessment;
import com.czintercity.icsec_app.assessment.entity.ControlStatus;
import com.czintercity.icsec_app.assessment.repository.AssessmentRepository;
import com.czintercity.icsec_app.assessment.repository.ControlStatusRepository;
import com.czintercity.icsec_app.controls.entity.Control;
import com.czintercity.icsec_app.controls.repository.ControlRepository;
import com.czintercity.icsec_app.topics.entity.Topic;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AssessmentService {

    private final ControlRepository controlRepository;
    private final ControlStatusRepository controlStatusRepository;
    private final AssessmentRepository assessmentRepository;

    public AssessmentService(ControlRepository controlRepository, ControlStatusRepository controlStatusRepository, AssessmentRepository assessmentRepository) {
        this.controlRepository = controlRepository;
        this.controlStatusRepository = controlStatusRepository;
        this.assessmentRepository = assessmentRepository;
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
}
