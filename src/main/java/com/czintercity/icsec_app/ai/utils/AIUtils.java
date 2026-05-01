package com.czintercity.icsec_app.ai.utils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AIUtils {

    private AIUtils() {}

    private static final Pattern JSON_ARRAY_PATTERN  = Pattern.compile("\\[.*\\]", Pattern.DOTALL);
    private static final Pattern JSON_OBJECT_PATTERN = Pattern.compile("\\{.*\\}", Pattern.DOTALL);

    /**
     * Strips Markdown code fences that Claude may mistakenly wrap around JSON output.
     *
     * @param raw raw Claude response
     * @return response with any leading/trailing code fences removed
     */
    public static String stripCodeFences(String raw) {
        String stripped = raw.strip();
        stripped = stripped.replaceAll("^```[a-zA-Z]*\\s*", "");
        stripped = stripped.replaceAll("```\\s*$", "");
        return stripped.strip();
    }

    /**
     * Attempts to extract a JSON structure from a raw AI response by regex.
     * Tries a JSON array ({@code [...]}) first, then falls back to a JSON object ({@code {...}}).
     *
     * @param raw raw AI response that may contain JSON mixed with other text
     * @return the extracted JSON string, or empty if neither structure was found
     */
    public static Optional<String> extractJson(String raw) {
        Matcher arrayMatcher = JSON_ARRAY_PATTERN.matcher(raw);
        if (arrayMatcher.find()) {
            return Optional.of(arrayMatcher.group());
        }
        Matcher objectMatcher = JSON_OBJECT_PATTERN.matcher(raw);
        if (objectMatcher.find()) {
            return Optional.of(objectMatcher.group());
        }
        return Optional.empty();
    }
}