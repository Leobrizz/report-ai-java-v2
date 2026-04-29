package com.acme.reportai.model;

import java.util.ArrayList;
import java.util.List;

public class FailureCluster {
    private String fingerprint;
    private String normalizedMessage;
    private String category;
    private String probableCause;
    private String recommendation;
    private final List<TestCaseResult> cases = new ArrayList<>();

    public String getFingerprint() { return fingerprint; }
    public void setFingerprint(String fingerprint) { this.fingerprint = fingerprint; }
    public String getNormalizedMessage() { return normalizedMessage; }
    public void setNormalizedMessage(String normalizedMessage) { this.normalizedMessage = normalizedMessage; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getProbableCause() { return probableCause; }
    public void setProbableCause(String probableCause) { this.probableCause = probableCause; }
    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
    public List<TestCaseResult> getCases() { return cases; }
}
