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
import java.util.ArrayList;
import java.util.List;

public class GeminiAiProvider implements AiProvider {
    private static final String DEFAULT_BASE_URL = "https://generativelanguage.googleapis.com/v1beta";
    private static final String DEFAULT_MODEL = "gemini-flash-latest";

    private final String baseUrl;
    private final String model;
    private final String apiKey;
    private final ObjectMapper mapper = new ObjectMapper();

    public GeminiAiProvider(String baseUrl, String model, String apiKey) {
        this.baseUrl = valueOrDefault(baseUrl, DEFAULT_BASE_URL);
        this.model = normalizeGeminiModel(valueOrDefault(model, DEFAULT_MODEL));
        this.apiKey = valueOrDefault(apiKey, System.getenv("GEMINI_API_KEY"));
    }

    @Override
    public AiClassification classify(FailureCluster cluster) {
        if (apiKey == null || apiKey.isBlank()) {
            return new MockAiProvider().classify(cluster);
        }

        for (String modelToTry : geminiModelsToTry(model)) {
            try {
                AiClassification classification = classifyWithModel(cluster, modelToTry);
                if (classification != null) return classification;
            } catch (Exception e) {
                if (!isTransientError(e.getMessage())) {
                    return new MockAiProvider().classify(cluster);
                }
            }
        }
        return new MockAiProvider().classify(cluster);
    }

    private AiClassification classifyWithModel(FailureCluster cluster, String modelToTry) throws Exception {
        String cleanBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String encodedModel = URLEncoder.encode(modelToTry, StandardCharsets.UTF_8);
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

        int code = conn.getResponseCode();
        String body = readBody(conn, code);
        if (code < 200 || code >= 300) {
            throw new IllegalStateException("HTTP " + code + " - " + compact(body));
        }

        JsonNode root = mapper.readTree(body);
        String response = root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText("");
        return parseResponse(response, cluster);
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

    private String readBody(HttpURLConnection conn, int code) {
        try (InputStream is = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream()) {
            if (is == null) return "";
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    private List<String> geminiModelsToTry(String selectedModel) {
        List<String> models = new ArrayList<>();
        addUnique(models, selectedModel);
        addUnique(models, "gemini-2.0-flash");
        addUnique(models, "gemini-2.0-flash-lite");
        addUnique(models, "gemini-1.5-flash");
        return models;
    }

    private void addUnique(List<String> values, String value) {
        if (value != null && !value.isBlank() && !values.contains(value)) values.add(value);
    }

    private boolean isTransientError(String message) {
        String m = message == null ? "" : message.toLowerCase();
        return m.contains("http 429") || m.contains("http 500") || m.contains("http 502") || m.contains("http 503") || m.contains("http 504") || m.contains("unavailable") || m.contains("high demand");
    }

    private static String valueOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String normalizeGeminiModel(String model) {
        return model.startsWith("models/") ? model.substring("models/".length()) : model;
    }

    private String compact(String value) {
        if (value == null) return "";
        String cleaned = value.replaceAll("\\s+", " ").trim();
        return cleaned.length() > 400 ? cleaned.substring(0, 400) + "..." : cleaned;
    }
}
