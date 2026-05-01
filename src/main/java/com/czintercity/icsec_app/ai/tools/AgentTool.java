package com.czintercity.icsec_app.ai.tools;

import com.anthropic.models.messages.Tool.*;

import java.util.Map;

/**
 * Abstract base class for all Claude agent tools.
 *
 * <p>Every concrete tool must declare its name, description, and input schema
 * (used when registering the tool with the Claude API), and implement
 * {@link #clank(Map)} to carry out the actual work.
 */
public abstract class AgentTool {

    // -----------------------------------------------------------------------
    // Identity & schema – used when registering the tool with Claude
    // -----------------------------------------------------------------------

    /**
     * Returns the unique snake_case name Claude will use to invoke this tool.
     * Example: {@code "validate_and_transform_json"}
     */
    public abstract String getName();

    /**
     * Returns a concise, human-readable description of what this tool does.
     * Claude uses this to decide when to call the tool.
     */
    public abstract String getDescription();

    /**
     * Returns a JSON Schema object that describes the tool's {@code input}
     * parameter block.  The map must be serializable to valid JSON and should
     * follow the structure expected by the Anthropic tool-use API, e.g.:
     *
     * <pre>{@code
     * {
     *   "type": "object",
     *   "properties": {
     *     "json_array": {
     *       "type": "string",
     *       "description": "A JSON array serialised as a string."
     *     }
     *   },
     *   "required": ["json_array"]
     * }
     * }</pre>
     */
    public abstract InputSchema getInputSchema();

    // -----------------------------------------------------------------------
    // Execution
    // -----------------------------------------------------------------------

    /**
     * Executes the tool with the given input parameters.
     *
     * @param input A map of parameter names to their values, matching the
     *              shape declared in {@link #getInputSchema()}.
     * @return A {@link ToolResult} that wraps either a successful output
     *         string or an error message.
     */
    public abstract ToolResult clank(Map<String, Object> input);

    // -----------------------------------------------------------------------
    // Lifecycle hooks (optional overrides)
    // -----------------------------------------------------------------------

    /**
     * Called once before the tool is first used.  Override to open connections,
     * load models, warm caches, etc.
     */
    public void initialize() {
        // no-op by default
    }

    /**
     * Called when the agent shuts down.  Override to release resources.
     */
    public void teardown() {
        // no-op by default
    }

    // -----------------------------------------------------------------------
    // toString
    // -----------------------------------------------------------------------

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{name='" + getName() + "'}";
    }

    // -----------------------------------------------------------------------
    // Nested result type
    // -----------------------------------------------------------------------

    /**
     * Immutable value object returned by {@link #clank(Map)}.
     */
    public static final class ToolResult {

        private final boolean success;
        private final String content;   // output on success, error message on failure

        private ToolResult(boolean success, String content) {
            this.success = success;
            this.content = content;
        }

        /** Factory – successful result. */
        public static ToolResult ok(String content) {
            return new ToolResult(true, content);
        }

        /** Factory – error result. */
        public static ToolResult error(String message) {
            return new ToolResult(false, message);
        }

        public boolean isSuccess() { return success; }

        /** The tool output (on success) or the error description (on failure). */
        public String getContent() { return content; }

        @Override
        public String toString() {
            return "ToolResult{success=" + success + ", content='" + content + "'}";
        }

    }

    /**
     * Fixes Claude mistakenly adding code fences.
     *
     * @param raw Raw claude response
     * @return Claude response without code fences.
     */
    protected static String stripCodeFences(String raw) {
        String stripped = raw.strip();
        // Remove opening fence: ```json or ```
        stripped = stripped.replaceAll("^```[a-zA-Z]*\\s*", "");
        // Remove closing fence
        stripped = stripped.replaceAll("```\\s*$", "");
        return stripped.strip();
    }
}