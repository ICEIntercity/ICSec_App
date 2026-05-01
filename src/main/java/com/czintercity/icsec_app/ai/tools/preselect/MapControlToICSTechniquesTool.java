package com.czintercity.icsec_app.ai.tools.preselect;

import com.anthropic.client.AnthropicClient;
import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.*;
import com.czintercity.icsec_app.ai.tools.AgentTool;
import com.czintercity.icsec_app.ai.utils.AIUtils;
import com.czintercity.icsec_app.attack.entity.Technique;
import com.czintercity.icsec_app.attack.repository.TechniqueRepository;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Agent tool that maps a security control to candidate MITRE ATT&CK for ICS techniques.
 *
 * <p>This tool acts as a pre-selection step in an agentic evaluation pipeline. Given a
 * security control title and description, it queries Claude (via the Anthropic API) to
 * identify which MITRE ATT&CK for ICS techniques the control may address. Techniques
 * are evaluated across five mapping categories:
 * <ul>
 *   <li><b>Detection</b> – the control helps detect attempted technique execution.</li>
 *   <li><b>Prevention</b> – the control prevents technique execution from succeeding.</li>
 *   <li><b>Deterrence</b> – the control influences adversary choice away from the technique.</li>
 *   <li><b>Containment</b> – the control limits the blast radius of successful execution.</li>
 *   <li><b>Recovery</b> – the control aids restoration or eradication after execution.</li>
 * </ul>
 *
 * <p>On construction, all known techniques are fetched from {@link TechniqueRepository} and
 * embedded into the Claude system prompt as a Markdown table. At invocation time, Claude
 * filters out any technique where it is 50% or more confident the technique is irrelevant
 * to the provided control, returning only the remaining candidates.
 *
 * <p>The tool is registered as a Spring {@code @Component} and is intended to be composed
 * into a larger agentic workflow rather than used standalone.
 *
 * @see AgentTool
 */
@Component
public class MapControlToICSTechniquesTool extends AgentTool {

    private final TechniqueRepository techniqueRepository;
    private static final Logger log = LoggerFactory.getLogger(MapControlToICSTechniquesTool.class);
    private static final Logger llmLog = LoggerFactory.getLogger("llm");

    private static final String TOOL_NAME = "map_control_to_ics_techniques";

    private static final String TOOL_DESCRIPTION =
            "Takes a security control title and description and returns a list of candidate MITRE ATT&CK for ICS techniques "
                    + "that the control may cover as a detective, preventative, deterrent, containment, or recovery control.";

    private static final String SYSTEM_PROMPT_HEADER =
            "You are a specialized ICS/OT cybersecurity architect. Your goal is to map security controls to candidate MITRE ATT&CK for ICS techniques.\n\n"
                    + "Mapping Categories:\n"
                    + "- Detective: Control helps detect attempted execution.\n"
                    + "- Preventative: Control prevents execution from succeeding.\n"
                    + "- Deterrent: Control influences choice away from the technique.\n"
                    + "- Containment: Control limits the impact of successful execution.\n"
                    + "- Recovery: Control aids in restoration or eradication.\n\n"
                    + "Refer to the following available MITRE ATT&CK for ICS techniques:";

    private static final String SYSTEM_PROMPT_FOOTER =
            "\nTask: Examine the provided list of techniques. Include ONLY techniques where you have at least 50% confidence that the technique is relevant and applicable to the provided security control.\n\n"
                    + "Respond ONLY with a JSON object containing a list 'candidate_techniques' of the matched techniques, where each item has: "
                    + "{ \"technique_id\": <string>, \"technique_title\": <string>, \"technique_description\": <string>, \"mapped_categories\": [<string>] }.\n"
                    + "If no techniques meet the 50% confidence threshold for relevance, return an empty list. Do not include any other text or preamble.";

    /**
     * The fully assembled Claude system prompt, built once at construction time from the
     * technique database. Reused across all invocations to avoid repeated database queries.
     */
    private String dynamicSystemPrompt;

    private static final String USER_PROMPT_TEMPLATE =
            "Identify candidate MITRE ATT&CK for ICS techniques for the following control:\n\n"
                    + "Control Title: %s\n"
                    + "Control Description: %s";

    private final AnthropicClient anthropicClient;
    private final ObjectMapper mapper;

    /**
     * Constructs the tool, injecting dependencies and eagerly building the Claude system prompt.
     *
     * <p>The system prompt is assembled once at startup by loading all techniques from
     * {@code TechniqueRepository} and embedding them as a Markdown table. This avoids
     * repeated database queries at inference time.
     *
     * @param anthropicClient     the Anthropic API client used to call Claude
     * @param techniqueRepository repository providing all available MITRE ATT&CK for ICS techniques
     * @param mapper              Jackson object mapper used for serialising/deserialising Claude's JSON response
     */
    public MapControlToICSTechniquesTool(AnthropicClient anthropicClient,
                                         TechniqueRepository techniqueRepository,
                                         ObjectMapper mapper) {
        this.techniqueRepository = techniqueRepository;
        this.anthropicClient = anthropicClient;
        this.mapper = mapper;
        this.buildMitreTechniques();
    }

    // --- Tool Implementation ---

    /** @return the tool identifier {@value TOOL_NAME}. */
    @Override
    public String getName() {
        return TOOL_NAME;
    }

    /** @return a human-readable description of this tool's purpose. */
    @Override
    public String getDescription() {
        return TOOL_DESCRIPTION;
    }

    /**
     * Defines the JSON input schema for this tool.
     *
     * <p>Required fields:
     * <ul>
     *   <li>{@code control_title} – short title of the security control.</li>
     *   <li>{@code control_description} – detailed description of the security control.</li>
     * </ul>
     *
     * @return the input schema describing the expected tool invocation payload
     */
    @Override
    @SuppressWarnings("unchecked")
    public Tool.InputSchema getInputSchema() {
        return Tool.InputSchema.builder()
                .properties(JsonValue.from(Map.of(
                        "control_title", Map.of(
                                "type", "string",
                                "description", "The title of the security control"),
                        "control_description", Map.of(
                                "type", "string",
                                "description", "The detailed description of the security control")
                )))
                .putAdditionalProperty("required", JsonValue.from(List.of("control_title", "control_description")))
                .build();
    }

    /**
     * Executes the technique-mapping logic for the given tool input.
     *
     * <p>Extracts {@code control_title} and {@code control_description} from {@code input},
     * delegates to {@link #callClaude(MappingInput)}, and returns the candidate techniques
     * serialised as a JSON string. Returns a {@link ToolResult#error} if any required field
     * is missing or blank, or if the Claude call or JSON serialisation fails.
     *
     * @param input map containing {@code control_title} and {@code control_description}
     * @return a {@link ToolResult} wrapping a JSON-serialised {@link MappingResponse} on
     *         success, or an error message on failure
     */
    @Override
    public ToolResult clank(Map<String, Object> input) {
        MDC.put("llm_component", TOOL_NAME);
        try {
            MappingInput mappingInput = new MappingInput();
            mappingInput.controlTitle = requireString(input, "control_title");
            mappingInput.controlDescription = requireString(input, "control_description");

            log.info("Mapping Control Title: " + mappingInput.controlTitle);
            llmLog.info("INPUT  control_title={}", mappingInput.controlTitle);

            MappingResponse output = callClaude(mappingInput);
            ToolResult result = ToolResult.ok(mapper.writeValueAsString(output));
            llmLog.info("OUTPUT {}", result.getContent());
            return result;
        } catch (Exception e) {
            llmLog.warn("ERROR  {}", e.getMessage());
            return ToolResult.error("Mapping failed: " + e.getMessage());
        } finally {
            MDC.remove("llm_component");
        }
    }

    /**
     * Sends the control details to Claude and parses the returned candidate techniques.
     *
     * <p>Constructs a {@link MessageCreateParams} using the pre-built system prompt and a
     * user message derived from {@code input}, invokes the Anthropic messages API, and
     * extracts the first text content block from the response. Any Markdown code fences
     * ({@code ```}) that Claude may have wrapped around the JSON are stripped via the
     * {@link AIUtils#stripCodeFences(String)} before deserialisation.
     *
     * @param input the control title and description to map
     * @return the parsed mapping response containing candidate techniques
     * @throws IllegalStateException if Claude returns no text content block
     * @throws Exception             if the API call fails or the response cannot be deserialised
     */
    private MappingResponse callClaude(MappingInput input) throws Exception {
        String userMessage = String.format(USER_PROMPT_TEMPLATE, input.controlTitle, input.controlDescription);

        MessageCreateParams params = MessageCreateParams.builder()
                .model(Model.CLAUDE_HAIKU_4_5)
                .maxTokens(8192)
                .system(this.dynamicSystemPrompt)
                .addUserMessage(userMessage)
                .build();

        Message response = anthropicClient.messages().create(params);
        String rawJson = response.content().stream()
                .filter(ContentBlock::isText)
                .map(block -> block.asText().text())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No response from AI"));

        log.info("LLM output: {}", rawJson);

        return mapper.readValue(AIUtils.stripCodeFences(rawJson), MappingResponse.class);
    }

    /**
     * Loads all MITRE ATT&CK for ICS techniques from the repository and builds the
     * Claude system prompt.
     *
     * <p>Techniques are formatted as a Markdown table wrapped in {@code <available_techniques>}
     * tags and embedded between {@link #SYSTEM_PROMPT_HEADER} and {@link #SYSTEM_PROMPT_FOOTER}.
     * Inline newlines in technique descriptions are stripped to preserve table formatting.
     * The result is stored in {@link #dynamicSystemPrompt} for reuse across all invocations.
     */
    private void buildMitreTechniques() {
        List<Technique> techniques = techniqueRepository.findAll();
        StringBuilder tableBuilder = new StringBuilder("\n<available_techniques>\n| MITRE ID | Title | Description |\n| :--- | :--- | :--- |\n");

            for (Technique technique : techniques) {
                String cleanDescription = technique.getDescription() != null
                        ? technique.getDescription().replace("\n", " ").replace("\r", "")
                        : "No description available";

                tableBuilder.append(String.format("| %s | %s | %s |\n",
                        technique.getMitreId(),
                        technique.getName(),
                    cleanDescription));
        }
        tableBuilder.append("</available_techniques>");

        this.dynamicSystemPrompt = SYSTEM_PROMPT_HEADER + tableBuilder.toString() + SYSTEM_PROMPT_FOOTER;
    }

    /**
     * Extracts a non-blank string value from a map, throwing if absent or blank.
     *
     * @param map the source map
     * @param key the key to look up
     * @return the string value for {@code key}
     * @throws IllegalArgumentException if the key is absent or its value is blank
     */
    private static String requireString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null || val.toString().isBlank()) throw new IllegalArgumentException("Missing field: " + key);
        return val.toString();
    }

    // --- Data Models ---

    /** Input DTO carrying the control title and description to be mapped. */
    public static final class MappingInput {
        @JsonProperty("control_title")       public String controlTitle;
        @JsonProperty("control_description") public String controlDescription;
    }

    /**
     * A single candidate MITRE ATT&CK for ICS technique, including the categories through
     * which the control is assessed to address it.
     */
    public static final class CandidateTechnique {
        @JsonProperty("technique_id")          public String techniqueId;
        @JsonProperty("technique_title")       public String techniqueTitle;
        @JsonProperty("technique_description") public String techniqueDescription;
        @JsonProperty("mapped_categories")     public List<String> mappedCategories;
    }

    /** Top-level response DTO wrapping the list of candidate techniques returned by Claude. */
    public static final class MappingResponse {
        @JsonProperty("candidate_techniques") public List<CandidateTechnique> candidateTechniques;
    }
}