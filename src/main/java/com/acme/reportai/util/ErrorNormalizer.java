package com.acme.reportai.util;

public class ErrorNormalizer {
    public String normalize(String message) {
        if (message == null || message.isBlank()) {
            return "sin_error";
        }
        return message.toLowerCase()
                .replaceAll("\\r|\\n", " ")
                .replaceAll("\\b\\d{3,}\\b", "<num>")
                .replaceAll("[a-f0-9]{8,}", "<hex>")
                .replaceAll("[^a-záéíóúñ0-9<> ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
