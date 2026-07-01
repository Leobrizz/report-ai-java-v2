package com.acme.reportai.ai;

public class PromptSanitizer {
    private PromptSanitizer() {}

    public static String compactError(String raw) {
        if (raw == null || raw.isBlank()) return "";
        String cleaned = raw
                .replaceAll("(?s)Capabilities \\{.*?\\}", "Capabilities{...}")
                .replaceAll("(?m)^\\s*at\\s+.*$", "")
                .replaceAll("Session ID: .*", "Session ID: ...")
                .replaceAll("Build info: .*", "Build info: ...")
                .replaceAll("System info: .*", "System info: ...")
                .replaceAll("Driver info: .*", "Driver info: ...")
                .replaceAll("Command: .*", "Command: ...")
                .replaceAll("[\\r\\n]+", "\n")
                .trim();
        return cleaned.length() > 2500 ? cleaned.substring(0, 2500) : cleaned;
    }
}
