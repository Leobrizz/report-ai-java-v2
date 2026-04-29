package com.acme.reportai.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class FingerprintGenerator {
    public String fingerprint(String normalizedText) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(normalizedText.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8 && i < digest.length; i++) {
                sb.append(String.format("%02x", digest[i]));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo generar fingerprint", e);
        }
    }
}
