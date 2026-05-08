package com.czintercity.icsec_app.assessment.service;

import com.czintercity.icsec_app.assessment.entity.Assessment;
import com.czintercity.icsec_app.assessment.entity.ControlStatus;
import com.czintercity.icsec_app.controls.entity.Control;
import com.czintercity.icsec_app.controls.repository.ControlRepository;
import com.czintercity.icsec_app.topics.entity.Topic;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AssessmentService {

    private final ControlRepository controlRepository;

    public AssessmentService(ControlRepository controlRepository) {
        this.controlRepository = controlRepository;
    }

    /**
     * Builds a mapping between all controls and their status in a given {@link Assessment}
     * @param assessment an {@link Assessment} object containing the sparse map of ratings
     * @return a Map containing all available controls and the status of each
     */
    public Map<Control, ControlStatus> buildControlStatusMap(Assessment assessment) {

        // Get the controls that are already assessed and have non-zero values
        Map<Control, ControlStatus> lookup = new HashMap<>();
        if(assessment.getControlStatusMapping() != null) {
            for (ControlStatus controlStatus : assessment.getControlStatusMapping()) {
                lookup.put(controlStatus.getControl(), controlStatus);
            }
        }

        // Get all controls
        Map<Control, ControlStatus> out = new HashMap<>();
        Iterable<Control> controls = controlRepository.findAll();

        // Go through all controls (to render them), and add either a blank assessment, or a full one if it exists
        for (Control control : controls) {
            ControlStatus status = lookup.get(control);
            if(status != null){
                // Add status
                out.put(control, status);
            }
            else {
                // Create a blank status mapping
                out.put(control, new ControlStatus(control, (short) 0, (short) 0));
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
     * @param assessment the {@link Assessment} whose saved statuses should be included
     * @return a {@link LinkedHashMap} of topics to their controls and statuses; insertion order is preserved
     * @see #buildControlStatusMap(Assessment)
     */
    public Map<Topic, Map<Control, ControlStatus>> buildDisplayMap(Assessment assessment) {
        Map<Topic, Map<Control, ControlStatus>> grouped = new LinkedHashMap<>();

        // Create the default map to group
        for (Map.Entry<Control, ControlStatus> entry : buildControlStatusMap(assessment).entrySet()) {
            Topic topic = entry.getKey().getTopic();

            // Create a bucket for the topic on first encounter
            if (!grouped.containsKey(topic)) {
                grouped.put(topic, new LinkedHashMap<>());
            }

            grouped.get(topic).put(entry.getKey(), entry.getValue());
        }

        return grouped;
    }
}
