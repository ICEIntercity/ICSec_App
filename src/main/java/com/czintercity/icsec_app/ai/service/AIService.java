package com.czintercity.icsec_app.ai.service;

import com.czintercity.icsec_app.ai.JsonArrayExtractorAgent;
import com.czintercity.icsec_app.ai.utils.AIUtils;
import com.czintercity.icsec_app.attack.entity.Technique;
import com.czintercity.icsec_app.attack.repository.TechniqueRepository;
import com.czintercity.icsec_app.controls.entity.Control;
import com.czintercity.icsec_app.relationships.techniqueCoverage.CoverageType;
import com.czintercity.icsec_app.relationships.techniqueCoverage.entity.DefaultTechniqueCoverage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service layer for AI-driven coverage assessment operations.
 *
 * <p>Provides the logic for converting the raw JSON output produced by
 * {@code CoverageAssessmentAgent} into {@link DefaultTechniqueCoverage} entities,
 * using a two-stage extraction pipeline:
 *
 * <ol>
 *   <li><b>Direct parse</b> — attempt to deserialise the provided JSON string
 *       immediately via {@link #parseAssessmentOutput(String, Control)}.</li>
 *   <li><b>AI extractor fallback</b> — if direct parsing fails, delegate to
 *       {@link JsonArrayExtractorAgent} to strip surrounding prose and code fences,
 *       then retry parsing via {@link #invokeExtractorAgent(String, Control)}.</li>
 *   <li><b>Hard failure</b> — if the AI extractor's output also cannot be parsed,
 *       an {@link IllegalStateException} is thrown with the original raw agent
 *       output attached, suitable for logging and diagnosis.</li>
 * </ol>
 *
 * <p>Technique lookup is performed against the local database via
 * {@link TechniqueRepository#findByMitreId(String)}. Techniques not found in the
 * database are skipped with a warning. Coverage type strings returned by the agent
 * are mapped case-insensitively to {@link CoverageType} enum values, defaulting to
 * {@link CoverageType#UNKNOWN} if no match is found.
 */
@Service
public class AIService {

    private static final Logger log = LoggerFactory.getLogger(AIService.class);

    private final JsonArrayExtractorAgent jsonArrayExtractorAgent;
    private final TechniqueRepository techniqueRepository;
    private final ObjectMapper objectMapper;

    public AIService(JsonArrayExtractorAgent jsonArrayExtractorAgent,
                     TechniqueRepository techniqueRepository,
                     ObjectMapper objectMapper) {
        this.jsonArrayExtractorAgent = jsonArrayExtractorAgent;
        this.techniqueRepository = techniqueRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Invokes {@link JsonArrayExtractorAgent} on the raw agent output to recover a
     * parseable JSON array, then delegates to {@link #parseAssessmentOutput(String, Control)}.
     *
     * <p>This method is the second-stage fallback in the extraction pipeline, called
     * when regex extraction either finds nothing or returns a string that fails to parse.
     *
     * @param agentJson the original raw string returned by the coverage assessment agent
     * @param control   the control being evaluated, used to populate returned entities
     * @return the parsed list of {@link DefaultTechniqueCoverage} entries
     * @throws IllegalStateException if the AI extractor's output also fails to parse,
     *                               with the original {@code agentJson} included in the message
     */
    public List<DefaultTechniqueCoverage> invokeExtractorAgent(String agentJson, Control control) {
        String extractedJson = jsonArrayExtractorAgent.clank(agentJson);
        extractedJson = AIUtils.stripCodeFences(extractedJson);
        log.info("AI extractor output: {}", extractedJson);
        try {
            return parseAssessmentOutput(extractedJson, control);
        } catch (Exception fallbackEx) {
            log.error("AI extractor fallback also failed. Raw agent output:\n{}", agentJson);
            throw new IllegalStateException(
                    "Failed to parse agent output after all extraction attempts. Raw output:\n" + agentJson,
                    fallbackEx);
        }
    }

    /**
     * Deserialises a JSON array string produced by the coverage assessment agent into
     * a list of {@link DefaultTechniqueCoverage} entities.
     *
     * <p>Each element of the array is expected to contain the following fields:
     * <ul>
     *   <li>{@code technique_id} — MITRE ATT&CK for ICS technique identifier (e.g. {@code "T0859"})</li>
     *   <li>{@code coverage_type} — coverage category string (e.g. {@code "detective"})</li>
     *   <li>{@code coverage_score} — integer rating from 1 to 5</li>
     *   <li>{@code reasoning} — plain-language justification for the score</li>
     * </ul>
     *
     * <p>Elements whose {@code technique_id} cannot be resolved in the database are
     * silently skipped. Elements with an unrecognised {@code coverage_type} are assigned
     * {@link CoverageType#UNKNOWN}.
     *
     * @param json    the JSON array string to parse
     * @param control the control to associate with each resulting coverage entity
     * @return a mutable list of populated {@link DefaultTechniqueCoverage} entities
     * @throws Exception if {@code json} cannot be deserialised as a JSON array
     */
    public List<DefaultTechniqueCoverage> parseAssessmentOutput(String json, Control control) throws Exception {
        List<Map<String, Object>> items = objectMapper.readValue(json, new TypeReference<>() {});
        List<DefaultTechniqueCoverage> result = new ArrayList<>();

        for (Map<String, Object> item : items) {
            String mitreId = (String) item.get("technique_id");
            String coverageTypeStr = (String) item.get("coverage_type");
            int score = ((Number) item.get("coverage_score")).intValue();
            String reasoning = (String) item.get("reasoning");

            Technique technique = techniqueRepository.findByMitreId(mitreId).orElse(null);
            if (technique == null) {
                log.warn("Technique not found in DB, skipping: {}", mitreId);
                continue;
            }

            CoverageType coverageType;
            try {
                coverageType = CoverageType.valueOf(coverageTypeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Unknown coverage type '{}', defaulting to UNKNOWN", coverageTypeStr);
                coverageType = CoverageType.UNKNOWN;
            }

            DefaultTechniqueCoverage coverage = new DefaultTechniqueCoverage();
            coverage.setControl(control);
            coverage.setTechnique(technique);
            coverage.setCoverageType(coverageType);
            coverage.setCoverageRating((short) score);
            coverage.setJustification(reasoning);

            result.add(coverage);
        }

        return result;
    }
}