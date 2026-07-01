package com.acme.reportai.ai;

import com.acme.reportai.model.AiClassification;
import com.acme.reportai.model.FailureCluster;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class OllamaAiProvider implements AiProvider {
    private final String baseUrl;
    private final String model;
    private final ObjectMapper mapper = new ObjectMapper();

    public OllamaAiProvider(String baseUrl, String model) {
        this.baseUrl = baseUrl == null || baseUrl.isBlank() ? "http://localhost:11434" : baseUrl;
        this.model = model == null || model.isBlank() ? "llama3.1" : model;
    }

    @Override
    public AiClassification classify(FailureCluster cluster) {
        try {
            URL url = new URL(baseUrl + "/api/generate");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(120000);
            conn.setDoOutput(true);

            String payload = mapper.createObjectNode()
                    .put("model", model)
                    .put("stream", false)
                    .put("prompt", buildPrompt(cluster))
                    .toString();

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }

            try (InputStream is = conn.getInputStream()) {
                JsonNode root = mapper.readTree(is);
                String response = root.path("response").asText("");
                return parseResponse(response, cluster);
            }
        } catch (Exception e) {
            return new MockAiProvider().classify(cluster);
        }
    }

    private String buildPrompt(FailureCluster cluster) {
        String msg = PromptSanitizer.compactError(cluster.getNormalizedMessage());
        return "Analiza este cluster de errores QA y responde SOLO JSON valido con category, probableCause, recommendation, summary.\n"
                + "Categoria previa: " + cluster.getCategory() + "\n"
                + "Casos afectados: " + cluster.getCases().size() + "\n"
                + "Error resumido:\n" + msg;
    }

    private AiClassification parseResponse(String response, FailureCluster cluster) {
        try {
            JsonNode node = mapper.readTree(response);
            AiClassification c = new AiClassification();
            c.setCategory(node.path("category").asText(cluster.getCategory()));
            c.setProbableCause(node.path("probableCause").asText("Causa probable no determinada"));
            c.setRecommendation(node.path("recommendation").asText("Revisar manualmente el cluster"));
            c.setSummary(node.path("summary").asText("Clasificacion por IA local"));
            return c;
        } catch (Exception e) {
            return new MockAiProvider().classify(cluster);
        }
    }
}
