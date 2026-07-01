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
import java.nio.charset.StandardCharsets;

public class OpenAiCompatibleProvider implements AiProvider {
    private final String baseUrl;
    private final String model;
    private final String apiKey;
    private final ObjectMapper mapper = new ObjectMapper();

    public OpenAiCompatibleProvider(String baseUrl, String model, String apiKey) {
        this.baseUrl = baseUrl;
        this.model = model;
        this.apiKey = apiKey;
    }

    @Override
    public AiClassification classify(FailureCluster cluster) {
        try {
            URL url = new URL(baseUrl + "/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(120000);
            if (apiKey != null && !apiKey.isBlank()) conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setDoOutput(true);

            String userPrompt = "Analiza este error QA y devuelve SOLO JSON con keys category, probableCause, recommendation, summary. "
                    + "Casos: " + cluster.getCases().size() + ". Mensaje: " + PromptSanitizer.compactError(cluster.getNormalizedMessage());

            ObjectNode rootRequest = mapper.createObjectNode();
            rootRequest.put("model", model);
            ArrayNode messages = rootRequest.putArray("messages");
            ObjectNode systemMessage = mapper.createObjectNode();
            systemMessage.put("role", "system");
            systemMessage.put("content", "Eres un analista QA. Devuelves solo JSON valido.");
            messages.add(systemMessage);
            ObjectNode userMessage = mapper.createObjectNode();
            userMessage.put("role", "user");
            userMessage.put("content", userPrompt);
            messages.add(userMessage);

            try (OutputStream os = conn.getOutputStream()) { os.write(mapper.writeValueAsBytes(rootRequest)); }
            try (InputStream is = conn.getInputStream()) {
                JsonNode root = mapper.readTree(is);
                String content = root.path("choices").path(0).path("message").path("content").asText("");
                JsonNode node = mapper.readTree(content);
                AiClassification c = new AiClassification();
                c.setCategory(node.path("category").asText(cluster.getCategory()));
                c.setProbableCause(node.path("probableCause").asText("Causa probable no determinada"));
                c.setRecommendation(node.path("recommendation").asText("Revisar cluster"));
                c.setSummary(node.path("summary").asText("Clasificacion via API compatible"));
                return c;
            }
        } catch (Exception e) {
            return new MockAiProvider().classify(cluster);
        }
    }
}
