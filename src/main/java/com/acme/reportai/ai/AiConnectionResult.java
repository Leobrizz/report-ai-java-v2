package com.acme.reportai.ai;

public class AiConnectionResult {
    private final boolean ok;
    private final String provider;
    private final String mode;
    private final String baseUrl;
    private final String model;
    private final String message;

    public AiConnectionResult(boolean ok, String provider, String mode, String baseUrl, String model, String message) {
        this.ok = ok;
        this.provider = provider;
        this.mode = mode;
        this.baseUrl = baseUrl;
        this.model = model;
        this.message = message;
    }

    public boolean isOk() { return ok; }
    public String getProvider() { return provider; }
    public String getMode() { return mode; }
    public String getBaseUrl() { return baseUrl; }
    public String getModel() { return model; }
    public String getMessage() { return message; }

    public String toLogLine() {
        String status = ok ? "OK" : "ERROR";
        return "[" + status + "] IA=" + provider + " | modo=" + mode + " | baseUrl=" + baseUrl + " | modelo=" + model + " | " + message;
    }
}
