package com.acme.reportai.ai;

import com.acme.reportai.model.AiClassification;
import com.acme.reportai.model.FailureCluster;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class GeminiAiProvider implements AiProvider {
    private static final String DEFAULT_BASE_URL = "https://generativelanguage.googleapis.com/v1beta";
    private static final String DEFAULT_MODEL = "gemini-flash-latest";

    private final String baseUrl;
    private final String model;
    private final String apiKey;
    private final ObjectMapper mapper = new ObjectMapper();

    public GeminiAiProvider(String baseUrl, String model, String apiKey) {
        this.baseUrl = valueOrDefault(baseUrl, DEFAULT_BASE_URL);
        this.model = valueOrDefault(model, DEFAULT_MODEL);
        this.apiKey = valueOrDefault(apiKey, System.getenv("GEMINI_API_KEY"));
    }

    @Override
    public AiClassification classify(FailureCluster cluster) {
        try {
            if (apiKey == null || apiKey.isBlank()) {
                throw new IllegalArgumentException("Falta GEMINI_API_KEY o --ai.apiKey");
            }

            String cleanBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
            String encodedModel = URLEncoder.encode(model, StandardCharsets.UTF_8);
            URL url = new URL(cleanBaseUrl + "/models/" + encodedModel + ":generateContent");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("X-goog-api-key", apiKey);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(120000);
            conn.setDoOutput(true);

            ObjectNode request = mapper.createObjectNode();
            ArrayNode contents = request.putArray("contents");
            ObjectNode content = mapper.createObjectNode();
            ArrayNode parts = content.putArray("parts");
            ObjectNode part = mapper.createObjectNode();
            part.put("text", buildPrompt(cluster));
            parts.add(part);
            contents.add(content);

            ObjectNode generationConfig = request.putObject("generationConfig");
            generationConfig.put("temperature", 0.2);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(mapper.writeValueAsBytes(request));
            }

            try (InputStream is = conn.getInputStream()) {
                JsonNode root = mapper.readTree(is);
                String response = root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText("");
                return parseResponse(response, cluster);
            }
        } catch (Exception e) {
            return new MockAiProvider().classify(cluster);
        }
    }

    private String buildPrompt(FailureCluster cluster) {
        return "Analiza este cluster de errores QA y responde SOLO JSON valido con category, probableCause, recommendation, summary.\n"
                + "Categoria previa: " + cluster.getCategory() + "\n"
                + "Casos afectados: " + cluster.getCases().size() + "\n"
                + "Error resumido:\n" + PromptSanitizer.compactError(cluster.getNormalizedMessage());
    }

    private AiClassification parseResponse(String response, FailureCluster cluster) {
        try {
            String json = extractJson(response);
            JsonNode node = mapper.readTree(json);
            AiClassification c = new AiClassification();
            c.setCategory(node.path("category").asText(cluster.getCategory()));
            c.setProbableCause(node.path("probableCause").asText("Causa probable no determinada"));
            c.setRecommendation(node.path("recommendation").asText("Revisar cluster"));
            c.setSummary(node.path("summary").asText("Clasificacion via Gemini"));
            return c;
        } catch (Exception e) {
            return new MockAiProvider().classify(cluster);
        }
    }

    private String extractJson(String response) {
        if (response == null) return "{}";
        String cleaned = response.trim();
        cleaned = cleaned.replace("```json", "").replace("```", "").trim();
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start >= 0 && end > start) return cleaned.substring(start, end + 1);
        return cleaned;
    }

    private static String valueOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
