package com.czintercity.icsec_app.ai;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Extracts a bare JSON array from an arbitrary LLM response string.
 *
 * <p>Entry point: {@link #clank(String)}.
 */
@Component
public class JsonArrayExtractorAgent {

    private static final Logger log = LoggerFactory.getLogger(JsonArrayExtractorAgent.class);

    private static final String SYSTEM_PROMPT =
            "You will receive text that contains a JSON array somewhere within it. "
            + "Find the JSON array and return it exactly as it appears — character-for-character, "
            + "with no modifications to its contents. "
            + "Do not add code fences, markdown, commentary, preamble, or any other text. "
            + "Your entire response must be the raw JSON array and only the JSON array, starting with '[' and ending with ']'. "
            + "If no JSON array is present, return exactly: []";

    private final AnthropicClient client;

    public JsonArrayExtractorAgent(AnthropicClient client) {
        this.client = client;
    }

    /**
     * Extracts the JSON array from the given LLM response string.
     *
     * @param rawInput the raw LLM response that should contain a JSON array
     * @return the bare JSON array string, or {@code []} if none was found
     */
    public String clank(String rawInput) {
        log.debug("Extracting JSON array from input ({} chars)", rawInput.length());

        MessageCreateParams params = MessageCreateParams.builder()
                .model(Model.CLAUDE_HAIKU_4_5)
                .maxTokens(8192)
                .system(SYSTEM_PROMPT)
                .addUserMessage(rawInput)
                .build();

        Message response = client.messages().create(params);

        String result = response.content().stream()
                .filter(ContentBlock::isText)
                .map(b -> b.asText().text())
                .reduce("", String::concat);

        log.debug("Extraction complete ({} chars)", result.length());
        return result;
    }
}