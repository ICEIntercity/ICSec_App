package com.czintercity.icsec_app.ai.controller;

import com.czintercity.icsec_app.ai.CoverageAssessmentAgent;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class AIController {

    private final CoverageAssessmentAgent coverageAssessmentAgent;
    private final JsonArrayExtractorAgent jsonArrayExtractorAgent;
    private final TechniqueRepository techniqueRepository;
    private final ObjectMapper objectMapper;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public AIController(CoverageAssessmentAgent coverageAssessmentAgent,
                        JsonArrayExtractorAgent jsonArrayExtractorAgent,
                        TechniqueRepository techniqueRepository,
                        ObjectMapper objectMapper) {
        this.coverageAssessmentAgent = coverageAssessmentAgent;
        this.jsonArrayExtractorAgent = jsonArrayExtractorAgent;
        this.techniqueRepository = techniqueRepository;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/ai/test")
    public ResponseEntity<String> testAI() {
        Control control = new Control();
        control.setName("Network-based firewalls");
        control.setDescription(
                "Network-based firewalls are dedicated network-mounted devices that inspect and filter network traffic. " +
                "Their implementations range from simple stateless firewalls, which filter traffic strictly based on a " +
                "static set of conditions (Source/Destination, port, protocol), through stateful and protocol-aware " +
                "firewalls, to next-generation firewalls (NGFW) that can also realise application-aware filtering " +
                "and/or intrusion detection features (which are covered under a separate control). Dedicated firewall " +
                "implementations also exist for common ICS protocols. In the context of ICS, the primary purpose of a " +
                "network-based firewall is to implement network access control, which restricts acceptable traffic for " +
                "each source and destination, and traffic filtering, which ensures that malicious communication patterns " +
                "do not get routed to destination. This can take either form of allowlists, for when the acceptable forms " +
                "of traffic can be exhaustively defined, or denylists, which are useful when not all acceptable traffic " +
                "can be defined, but various types of unacceptable traffic can. In addition, firewalls are a common " +
                "method of establishing logical network segmentation. A common notable example of this is separating OT " +
                "networks from the wider IT environment by restricting traffic between them.");

        String agentJson = coverageAssessmentAgent.clank(control);
        log.info("Agent raw output: {}", agentJson);

        List<DefaultTechniqueCoverage> coverages;

        // Stage 1: regex extraction
        Optional<String> regexExtracted = AIUtils.extractJson(agentJson);
        if (regexExtracted.isPresent()) {
            log.info("Regex extracted JSON ({} chars)", regexExtracted.get().length());
            try {
                coverages = parseAgentOutput(regexExtracted.get(), control);
            } catch (Exception regexParseEx) {
                log.warn("Regex-extracted JSON failed to parse, falling back to AI extractor: {}", regexParseEx.getMessage());
                coverages = aiExtractorFallback(agentJson, control);
            }
        } else {
            log.warn("Regex extraction found no JSON structure, falling back to AI extractor");
            coverages = aiExtractorFallback(agentJson, control);
        }

        log.info("Parsed {} coverage entries", coverages.size());

        StringBuilder sb = new StringBuilder();
        for (DefaultTechniqueCoverage c : coverages) {
            sb.append(c.getTechnique().getMitreId())
              .append(" | ").append(c.getCoverageType())
              .append(" | score=").append(c.getCoverageRating())
              .append(" | ").append(c.getJustification())
              .append("\n\n");
        }

        return ResponseEntity.ok(sb.isEmpty() ? agentJson : sb.toString());
    }

    private List<DefaultTechniqueCoverage> aiExtractorFallback(String agentJson, Control control) {
        String extractedJson = jsonArrayExtractorAgent.clank(agentJson);
        extractedJson = AIUtils.stripCodeFences(extractedJson); // Get rid of code fences (Claude insists on them for some stupid reason)
        log.info("AI extractor output: {}", extractedJson);
        try {
            return parseAgentOutput(extractedJson, control);
        } catch (Exception fallbackEx) {
            log.error("AI extractor fallback also failed. Raw agent output:\n{}", agentJson);
            throw new IllegalStateException(
                    "Failed to parse agent output after all extraction attempts. Raw output:\n" + agentJson,
                    fallbackEx);
        }
    }

    private List<DefaultTechniqueCoverage> parseAgentOutput(String json, Control control) throws Exception {
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