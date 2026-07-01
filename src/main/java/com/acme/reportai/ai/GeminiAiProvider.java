package com.acme.reportai.ai;

public class GeminiAiProvider extends OpenAiCompatibleProvider {
    private static final String DEFAULT_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/openai";
    private static final String DEFAULT_MODEL = "gemini-3.5-flash";

    public GeminiAiProvider(String baseUrl, String model, String apiKey) {
        super(valueOrDefault(baseUrl, DEFAULT_BASE_URL),
                valueOrDefault(model, DEFAULT_MODEL),
                valueOrDefault(apiKey, System.getenv("GEMINI_API_KEY")));
    }

    private static String valueOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
