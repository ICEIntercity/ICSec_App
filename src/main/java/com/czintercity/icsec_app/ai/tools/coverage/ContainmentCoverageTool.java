package com.czintercity.icsec_app.ai.tools.coverage;

import com.anthropic.client.AnthropicClient;
import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.*;
import com.czintercity.icsec_app.ai.tools.AgentTool;
import com.czintercity.icsec_app.ai.utils.AIUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Evaluates how well a security control covers a given attack technique.
 *
 * <p>Extends {@link AgentTool} and is designed to be registered with an
 * asynchronous Claude agent loop. The agent calls {@link #clank(Map)}
 * whenever Claude emits a {@code tool_use} block whose name matches
 * {@link #getName()}.
 */
@Component
public class ContainmentCoverageTool extends AgentTool {

    private static final Model CLAUDE_MODEL = Model.CLAUDE_HAIKU_4_5;
    private static final String TOOL_NAME = "evaluate_containment_coverage";

    private static final String TOOL_DESCRIPTION =
            "Evaluates how well a given security control contains/mitigates the impact of a MITRE ATT&CK for ICS technique. "
                    + "Returns a numeric coverage rating (0-5) and a plain-language justification.";


    private static final String SYSTEM_PROMPT =
            "You are a cybersecurity analyst. "
                    + "Your task is to evaluate how well a security control contains the spread and reduces the blast radius of a MITRE ATT&CK for ICS technique. "
                    + "Impact and blast radius are to be considered primarily in terms of operational disruption to the environment, secondarily by scale of compromise and success of attacker goals. "
                    + "Your evaluation will assume best-in-class coverage across the ICS environment and comprise of a coverage rating and a human-readable text justification for your rating. "
                    + "The coverage rating can be one of the following:\n"
                    + "0: The control does not meaningfully reduce the blast radius of the given technique.\n"
                    + "1: The control may reduce the blast radius of the given technique under specific unlikely circumstances, or make impact marginally more complicated to an attacker.\n"
                    + "2: The control is sufficient to reduce the possible impact of the given technique by 25 to 50% in a typical OT environment. \n"
                    + "3: The control is sufficient to reduce the possible impact of the given technique by 50 to 75% in a typical OT environment. \n"
                    + "4: The control is sufficient to reduce the possible impact of the given technique by 75 to 90% in a typical OT environment. \n"
                    + "5: The control fully prevents operational disruption or impact on adjacent systems except in edge cases and/or under unforeseen circumstances.\n"
                    + "If the control does not address this technique in any meaningful way, assign a rating of 0.\n"
                    + "The coverage_justification must explain your reasoning: describe which specific aspects of the control do or do not address the technique, and why you assigned the given rating.\n"
                    + "Respond ONLY with a JSON object matching this schema: "
                    + "{ \"coverage_rating\": <integer 0-5>, \"coverage_justification\": <string> }. "
                    + "Do not include any other text.";

    private static final String USER_PROMPT_TEMPLATE =
            "Evaluate how well the following control covers the attack technique.\n\n"
                    + "Technique ID   : %s\n"
                    + "Technique Name : %s\n"
                    + "Technique Desc : %s\n\n"
                    + "Control Name   : %s\n"
                    + "Control Desc   : %s";

    public ContainmentCoverageTool(AnthropicClient anthropicClient, ObjectMapper mapper) {
        this.anthropicClient = anthropicClient;
        this.mapper = mapper;
    }

    /** Structured representation of the validated tool input. */
    public static final class CoverageInput {
        @JsonProperty("technique_id")          public String techniqueId;
        @JsonProperty("technique_name")         public String techniqueName;
        @JsonProperty("technique_description")  public String techniqueDescription;
        @JsonProperty("control_name")            public String controlName;
        @JsonProperty("control_description")     public String controlDescription;
    }

    /** Structured representation of the tool output. */
    public static final class CoverageOutput {
        @JsonProperty("coverage_rating")        public int    coverageRating;
        @JsonProperty("coverage_justification") public String coverageJustification;
    }

    // -----------------------------------------------------------------------
    // Fields
    // -----------------------------------------------------------------------

    private final AnthropicClient anthropicClient;
    private final ObjectMapper mapper;


    // -----------------------------------------------------------------------
    // AgentTool – identity & schema
    // -----------------------------------------------------------------------

    @Override
    public String getName() {
        return TOOL_NAME;
    }

    @Override
    public String getDescription() {
        return TOOL_DESCRIPTION;
    }

    /**
     * Returns the JSON Schema for the tool's input block, in the shape
     * expected by the Anthropic tool-use API.
     */
    @Override
    public Tool.InputSchema getInputSchema() {
        return Tool.InputSchema.builder()
                .properties(JsonValue.from(Map.of(
                        "technique_id", Map.of(
                                "type", "string",
                                "description", "ATT&CK technique identifier, e.g. T0859"),
                        "technique_name", Map.of(
                                "type", "string",
                                "description", "Human-readable technique name"),
                        "technique_description", Map.of(
                                "type", "string",
                                "description", "Full description of the attack technique"),
                        "control_name", Map.of(
                                "type", "string",
                                "description", "Name of the security control being evaluated"),
                        "control_description", Map.of(
                                "type", "string",
                                "description", "Full description of the security control")
                )))
                .putAdditionalProperty("required", JsonValue.from(List.of(
                        "technique_id",
                        "technique_name",
                        "technique_description",
                        "control_name",
                        "control_description"
                )))
                .build();
    }

    // -----------------------------------------------------------------------
    // AgentTool – execution
    // -----------------------------------------------------------------------

    /**
     * Validates the input map, calls Claude, and returns the coverage rating.
     *
     * <p>The agent loop should pass the {@code input} field from Claude's
     * {@code tool_use} block (already parsed to a {@code Map}) and forward
     * the returned {@link ToolResult#getContent()} as a {@code tool_result}
     * message.
     *
     * @param input map of parameter names to values, as supplied by Claude
     * @return {@link ToolResult#ok(String)} with serialised {@link CoverageOutput}
     *         JSON, or {@link ToolResult#error(String)} on failure
     */
    @Override
    public ToolResult clank(Map<String, Object> input) {
        try {
            CoverageInput coverageInput = parseInput(input);
            CoverageOutput output = callClaude(coverageInput);
            return ToolResult.ok(mapper.writeValueAsString(output));
        } catch (IllegalArgumentException e) {
            return ToolResult.error("Invalid input: " + e.getMessage());
        } catch (Exception e) {
            return ToolResult.error("Tool execution failed: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    /** Pulls fields from the raw map and validates that none are blank. */
    private CoverageInput parseInput(Map<String, Object> input) {
        CoverageInput ci = new CoverageInput();
        ci.techniqueId          = requireString(input, "technique_id");
        ci.techniqueName        = requireString(input, "technique_name");
        ci.techniqueDescription = requireString(input, "technique_description");
        ci.controlName          = requireString(input, "control_name");
        ci.controlDescription   = requireString(input, "control_description");
        return ci;
    }

    private static String requireString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null || value.toString().isBlank()) {
            throw new IllegalArgumentException("Missing or blank field: " + key);
        }
        return value.toString();
    }

    /** Sends the structured input to Claude and parses the JSON response. */
    private CoverageOutput callClaude(CoverageInput input) throws Exception {
        String userMessage = String.format(
                USER_PROMPT_TEMPLATE,
                input.techniqueId,
                input.techniqueName,
                input.techniqueDescription,
                input.controlName,
                input.controlDescription
        );

        MessageCreateParams params = MessageCreateParams.builder()
                .model(CLAUDE_MODEL)
                .maxTokens(1024)
                .system(SYSTEM_PROMPT)
                .addUserMessage(userMessage)
                .build();

        Message response = anthropicClient.messages().create(params);

        String rawJson = null;

        List<ContentBlock> blocks = response.content();
        for (ContentBlock block : blocks) {
            if (block.isText()) {
                rawJson = block.asText().text();
                break; // Take the first text block found
            }
        }

        if (rawJson == null) {
            throw new IllegalStateException("No text content in Claude response");
        }

        return mapper.readValue(AIUtils.stripCodeFences(rawJson), CoverageOutput.class);
    }
}