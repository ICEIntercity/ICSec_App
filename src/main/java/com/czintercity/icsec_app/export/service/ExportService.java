package com.czintercity.icsec_app.export.service;

import com.czintercity.icsec_app.controls.entity.Control;
import com.czintercity.icsec_app.controls.repository.ControlRepository;
import com.czintercity.icsec_app.export.dto.ControlExportDto;
import com.czintercity.icsec_app.export.dto.ExportDto;
import com.czintercity.icsec_app.export.dto.RelationshipExportDto;
import com.czintercity.icsec_app.export.dto.TechniqueCoverageExportDto;
import com.czintercity.icsec_app.export.dto.TechniqueExportDto;
import com.czintercity.icsec_app.export.dto.TopicExportDto;
import com.czintercity.icsec_app.relationships.controlRelationship.entity.ControlRelationship;
import com.czintercity.icsec_app.relationships.techniqueCoverage.entity.TechniqueCoverage;
import com.czintercity.icsec_app.topics.entity.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds a full export of all {@link Control}s together with their child entities
 * (technique coverage and relationships) as a tree of plain DTOs suitable for JSON serialization.
 */
@Service
public class ExportService {
    private static final Logger log = LoggerFactory.getLogger(ExportService.class);

    private final ControlRepository controlRepository;

    public ExportService(ControlRepository controlRepository) {
        this.controlRepository = controlRepository;
    }

    /**
     * Exports every control and its child entities. Each relationship is included once, under its
     * source control's outgoing relationships, so the whole relationship graph is captured without
     * duplication.
     *
     * @return an {@link ExportDto} holding the export date and all exported controls
     */
    @Transactional(readOnly = true)
    public ExportDto exportAll() {
        log.info("Building full export of all controls.");

        List<ControlExportDto> controls = new ArrayList<>();
        for (Control control : controlRepository.findAll()) {
            controls.add(toControlDto(control));
        }

        log.info("Export built with {} control(s).", controls.size());
        return new ExportDto(Instant.now().toString(), controls);
    }

    private ControlExportDto toControlDto(Control control) {
        List<TechniqueCoverageExportDto> coverage = control.getTechniqueCoverage().stream()
                .map(this::toCoverageDto)
                .toList();

        List<ControlRelationship> outgoing = control.getOutgoingRelationships();
        List<RelationshipExportDto> relationships = (outgoing == null ? List.<ControlRelationship>of() : outgoing).stream()
                .map(this::toRelationshipDto)
                .toList();

        List<String> references = control.getReferences() == null
                ? List.of()
                : List.copyOf(control.getReferences());

        return new ControlExportDto(
                control.getId(),
                control.getDisplayId(),
                control.getCode(),
                control.getName(),
                control.getDescription(),
                control.getCostIndex(),
                references,
                toTopicDto(control.getTopic()),
                coverage,
                relationships
        );
    }

    private TopicExportDto toTopicDto(Topic topic) {
        if (topic == null) {
            return null;
        }
        return new TopicExportDto(
                topic.getId(),
                topic.getCode(),
                topic.getName(),
                topic.getDescription(),
                topic.getColor()
        );
    }

    private TechniqueCoverageExportDto toCoverageDto(TechniqueCoverage coverage) {
        TechniqueExportDto technique = coverage.getTechnique() == null
                ? null
                : new TechniqueExportDto(
                        coverage.getTechnique().getId(),
                        coverage.getTechnique().getMitreId(),
                        coverage.getTechnique().getName()
                );

        return new TechniqueCoverageExportDto(
                coverage.getId(),
                technique,
                coverage.getCoverageType(),
                coverage.getCoverageRating(),
                coverage.getJustification()
        );
    }

    private RelationshipExportDto toRelationshipDto(ControlRelationship relationship) {
        return new RelationshipExportDto(
                relationship.getId(),
                relationship.getType(),
                relationship.getSource() == null ? null : relationship.getSource().getId(),
                relationship.getTarget() == null ? null : relationship.getTarget().getId()
        );
    }
}