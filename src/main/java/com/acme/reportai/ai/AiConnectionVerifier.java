package com.acme.reportai.ai;

import com.acme.reportai.cli.AppConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class AiConnectionVerifier {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta";
    private static final String GEMINI_MODEL = "gemini-flash-latest";
    private static final String OLLAMA_BASE_URL = "http://localhost:11434";
    private static final String OLLAMA_MODEL = "llama3.1";

    private AiConnectionVerifier() {}

    public static AiConnectionResult verify(AppConfig config) {
        String mode = valueOrDefault(config.getAiMode(), "mock").toLowerCase();
        return switch (mode) {
            case "gemini" -> verifyGemini(config);
            case "ollama", "hollama" -> verifyOllama(config, mode);
            case "openai" -> verifyOpenAiCompatible(config, "OpenAI compatible", mode);
            case "codex" -> verifyOpenAiCompatible(config, "Codex", mode);
            default -> ok("Mock", "mock", "local", "mock", "Modo sin conexion externa. Se usa clasificacion simulada.");
        };
    }

    private static AiConnectionResult verifyGemini(AppConfig config) {
        String baseUrl = valueOrDefault(config.getAiBaseUrl(), GEMINI_BASE_URL);
        String model = normalizeGeminiModel(valueOrDefault(config.getAiModel(), GEMINI_MODEL));
        String apiKey = valueOrDefault(config.getAiApiKey(), System.getenv("GEMINI_API_KEY"));
        if (apiKey == null || apiKey.isBlank()) {
            return fail("Gemini", "gemini", baseUrl, model, "Falta API key. Cargala en la UI o setea GEMINI_API_KEY.");
        }

        try {
            String cleanBaseUrl = trimSlash(baseUrl);
            String encodedModel = URLEncoder.encode(model, StandardCharsets.UTF_8);
            URL url = new URL(cleanBaseUrl + "/models/" + encodedModel + ":generateContent");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("X-goog-api-key", apiKey);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);
            conn.setDoOutput(true);

            ObjectNode request = mapper.createObjectNode();
            ArrayNode contents = request.putArray("contents");
            ObjectNode content = mapper.createObjectNode();
            ArrayNode parts = content.putArray("parts");
            ObjectNode part = mapper.createObjectNode();
            part.put("text", "Responde solamente OK");
            parts.add(part);
            contents.add(content);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(mapper.writeValueAsBytes(request));
            }

            int code = conn.getResponseCode();
            String body = readBody(conn, code);
            if (code >= 200 && code < 300) {
                return ok("Gemini", "gemini", baseUrl, model, "Conexion verificada correctamente con generateContent.");
            }
            return fail("Gemini", "gemini", baseUrl, model, "HTTP " + code + " - " + compact(body));
        } catch (Exception e) {
            return fail("Gemini", "gemini", baseUrl, model, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private static AiConnectionResult verifyOllama(AppConfig config, String mode) {
        String baseUrl = valueOrDefault(config.getAiBaseUrl(), OLLAMA_BASE_URL);
        String model = valueOrDefault(config.getAiModel(), OLLAMA_MODEL);
        try {
            URL url = new URL(trimSlash(baseUrl) + "/api/tags");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(15000);
            int code = conn.getResponseCode();
            String body = readBody(conn, code);
            if (code >= 200 && code < 300) {
                String extra = body.contains(model) ? "Modelo encontrado en Ollama." : "Servidor OK. Si el modelo no existe, ejecuta: ollama pull " + model;
                return ok("Ollama", mode, baseUrl, model, extra);
            }
            return fail("Ollama", mode, baseUrl, model, "HTTP " + code + " - " + compact(body));
        } catch (Exception e) {
            return fail("Ollama", mode, baseUrl, model, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private static AiConnectionResult verifyOpenAiCompatible(AppConfig config, String provider, String mode) {
        String baseUrl = config.getAiBaseUrl();
        String model = config.getAiModel();
        String apiKey = valueOrDefault(config.getAiApiKey(), System.getenv("OPENAI_API_KEY"));
        if (baseUrl == null || baseUrl.isBlank()) return fail(provider, mode, "-", valueOrDefault(model, "-"), "Falta Base URL IA.");
        if (model == null || model.isBlank()) return fail(provider, mode, baseUrl, "-", "Falta modelo.");
        try {
            URL url = new URL(trimSlash(baseUrl) + "/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            if (apiKey != null && !apiKey.isBlank()) conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);
            conn.setDoOutput(true);

            ObjectNode root = mapper.createObjectNode();
            root.put("model", model);
            root.put("max_tokens", 5);
            ArrayNode messages = root.putArray("messages");
            ObjectNode user = mapper.createObjectNode();
            user.put("role", "user");
            user.put("content", "Responde solamente OK");
            messages.add(user);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(mapper.writeValueAsBytes(root));
            }

            int code = conn.getResponseCode();
            String body = readBody(conn, code);
            if (code >= 200 && code < 300) return ok(provider, mode, baseUrl, model, "Conexion verificada correctamente con /chat/completions.");
            return fail(provider, mode, baseUrl, model, "HTTP " + code + " - " + compact(body));
        } catch (Exception e) {
            return fail(provider, mode, baseUrl, model, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private static String readBody(HttpURLConnection conn, int code) {
        try (InputStream is = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream()) {
            if (is == null) return "";
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    private static AiConnectionResult ok(String provider, String mode, String baseUrl, String model, String message) {
        return new AiConnectionResult(true, provider, mode, baseUrl, model, message);
    }

    private static AiConnectionResult fail(String provider, String mode, String baseUrl, String model, String message) {
        return new AiConnectionResult(false, provider, mode, baseUrl, model, message);
    }

    private static String valueOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String trimSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private static String normalizeGeminiModel(String model) {
        return model.startsWith("models/") ? model.substring("models/".length()) : model;
    }

    private static String compact(String value) {
        if (value == null) return "";
        String cleaned = value.replaceAll("\\s+", " ").trim();
        return cleaned.length() > 400 ? cleaned.substring(0, 400) + "..." : cleaned;
    }
}
