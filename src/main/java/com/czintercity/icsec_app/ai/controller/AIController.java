package com.czintercity.icsec_app.ai.controller;

import com.czintercity.icsec_app.ai.CoverageAssessmentAgent;
import com.czintercity.icsec_app.ai.service.AIService;
import com.czintercity.icsec_app.ai.utils.AIUtils;
import com.czintercity.icsec_app.controls.entity.Control;
import com.czintercity.icsec_app.relationships.techniqueCoverage.entity.TechniqueCoverage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Controller
public class AIController {

    private final CoverageAssessmentAgent coverageAssessmentAgent;
    private final AIService aiService;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public AIController(CoverageAssessmentAgent coverageAssessmentAgent,
                        AIService aiService) {
        this.coverageAssessmentAgent = coverageAssessmentAgent;
        this.aiService = aiService;
    }

    /**
     * Asynchronous endpoint to assess a control's coverage via Ajax.
     * Takes a Control object, processes it through the AI agent, and returns
     * a template fragment containing the resulting coverage list.
     */
    @PostMapping("/ai/assess-coverage")
    public String assessControlCoverage(@RequestBody java.util.Map<String, String> payload, Model model) {
        String name = payload.get("name");
        String description = payload.get("description");

        // Create a transient control object to pass to existing logic
        Control control = new Control();
        control.setName(name);
        control.setDescription(description);

        log.info("Starting AI assessment from ephemeral control object `{}`", name);

        String agentJson = coverageAssessmentAgent.clank(control);
        List<TechniqueCoverage> coverages = extractAndParse(agentJson, control);

        model.addAttribute("coverageList", coverages);
        model.addAttribute("controlName", name);

        return "fragments/ai :: coverageTable";
    }

    /**
     * Test endpoint that loads pre-computed assessment output from
     * {@code resources/test/assessment_out.json} and renders the coverage
     * fragment without making any AI calls.
     */
    @PostMapping("/ai/test-render")
    public String testRender(@RequestBody java.util.Map<String, String> payload, Model model) throws IOException {
        String name = payload.get("name");
        String description = payload.get("description");

        // Create a transient control object for the parser
        Control control = new Control();
        control.setName(name);
        control.setDescription(description);

        ClassPathResource resource = new ClassPathResource("test/assessment_out.json");
        String json = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        log.info("Loaded test assessment JSON ({} chars)", json.length());

        List<TechniqueCoverage> coverages = extractAndParse(json, control);

        model.addAttribute("coverageList", coverages);
        model.addAttribute("controlName", name);

        return "fragments/ai :: coverageTable";
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

        List<TechniqueCoverage> coverages = extractAndParse(agentJson, control);
        log.info("Parsed {} coverage entries", coverages.size());

        StringBuilder sb = new StringBuilder();
        for (TechniqueCoverage c : coverages) {
            sb.append(c.getTechnique().getMitreId())
              .append(" | ").append(c.getCoverageType())
              .append(" | score=").append(c.getCoverageRating())
              .append(" | ").append(c.getJustification())
              .append("\n\n");
        }

        return ResponseEntity.ok(sb.isEmpty() ? agentJson : sb.toString());
    }

    private List<TechniqueCoverage> extractAndParse(String rawJson, Control control) {
        Optional<String> regexExtracted = AIUtils.extractJson(rawJson);
        if (regexExtracted.isPresent()) {
            try {
                return aiService.parseAssessmentOutput(regexExtracted.get(), control);
            } catch (Exception e) {
                log.warn("Regex-extracted JSON failed to parse, falling back to AI extractor: {}", e.getMessage());
                return aiService.invokeExtractorAgent(rawJson, control);
            }
        } else {
            log.warn("No JSON found by regex, falling back to AI extractor");
            return aiService.invokeExtractorAgent(rawJson, control);
        }
    }
}
